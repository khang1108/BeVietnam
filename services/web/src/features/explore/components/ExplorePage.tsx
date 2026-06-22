'use client';

import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import type { Map, Marker } from '@goongmaps/goong-js';
import { Crosshair, List, MagnifyingGlass, Star, X } from '@phosphor-icons/react';
import styles from '../styles/explore.module.css';
import { weatherApi, nearbyApi, type WeatherCondition as ApiWeatherCondition, type NearbyPlace } from '@/lib/api';

// ─── Types ────────────────────────────────────────────────────────────────────

type WeatherCondition = 'sunny' | 'cloudy' | 'rainy';
type Category = 'history' | 'culture' | 'food' | 'nature' | 'festival' | 'amusement' | 'lodging' | 'place';

interface Place {
    id: string;
    title: string;
    subtitle: string;
    description: string;
    category: Category;
    lat: number;
    lng: number;
    weather: WeatherCondition;
    rating: number;
    region: string;
}

interface CityHub {
    id: string;
    name: string;
    lat: number;
    lng: number;
    count: number;
}

type GeoJsonSourceLike = {
    setData: (data: unknown) => void;
};

type ExploreLayerSpec = {
    id: string;
    [key: string]: unknown;
};

type ExploreMap = Map & {
    getSource?: (sourceId: string) => GeoJsonSourceLike | undefined;
    addSource?: (sourceId: string, source: unknown) => void;
    getLayer?: (layerId: string) => unknown;
    addLayer?: (layer: ExploreLayerSpec) => void;
    getCanvas?: () => HTMLCanvasElement;
};

type ExploreInteractiveMap = ExploreMap & {
    on: (eventName: string, layerId: string, handler: (event: ExploreLayerClickEvent) => void) => unknown;
    off: (eventName: string, layerId: string, handler: (event: ExploreLayerClickEvent) => void) => unknown;
};

type ExploreLayerClickEvent = {
    features?: Array<{
        properties?: {
            id?: string;
        };
    }>;
};

// ─── Constants ────────────────────────────────────────────────────────────────

// Only fetch/show live POIs when zoomed in enough to bound the viewport sensibly.
const NEARBY_MIN_ZOOM = 13;

// Pin colour per category (the teardrop fill — Google-style)
const CATEGORY_COLORS: Record<Category, string> = {
    history:  '#e6b422',
    culture:  '#c9302c',
    food:     '#f97316',
    nature:   '#2d9d6f',
    festival: '#d946ef',
    amusement:'#3b82f6',
    lodging:  '#6366f1',
    place:    '#9ca3af',
};

const CATEGORY_SHORT: Record<Category, string> = {
    history:  'D',
    culture:  'V',
    food:     'A',
    nature:   'T',
    festival: 'L',
    amusement:'G',
    lodging:  'N',
    place:    'P',
};

const WEATHER_LABELS: Record<WeatherCondition, string> = {
    sunny:  'Nắng',
    cloudy: 'Có mây',
    rainy:  'Mưa',
};

const CATEGORY_LABEL: Record<Category, string> = {
    history:  'Di tích',
    culture:  'Văn hóa',
    food:     'Ẩm thực',
    nature:   'Thiên nhiên',
    festival: 'Lễ hội',
    amusement:'Vui chơi',
    lodging:  'Lưu trú',
    place:    'Khác',
};

const FILTER_CHIPS = [
    { key: 'all',       label: 'Tất cả' },
    { key: 'history',   label: 'Di tích' },
    { key: 'culture',   label: 'Văn hóa' },
    { key: 'food',      label: 'Ẩm thực' },
    { key: 'lodging',   label: 'Lưu trú' },
    { key: 'nature',    label: 'Thiên nhiên' },
    { key: 'amusement', label: 'Vui chơi' },
    { key: 'festival',  label: 'Lễ hội' },
];

const MOCK_PLACES: Place[] = [
    // ── Huế ──
    { id: '1', title: 'Đại Nội Huế', subtitle: 'Hoàng thành triều Nguyễn',
        description: 'Quần thể cung điện rộng lớn của 13 đời vua nhà Nguyễn. Ngọ Môn, Điện Thái Hòa và Tử Cấm Thành là những điểm không thể bỏ lỡ.',
        category: 'history', lat: 16.4698, lng: 107.5796, weather: 'sunny', rating: 4.8, region: 'Huế' },
    { id: '2', title: 'Chùa Thiên Mụ', subtitle: 'Ngôi chùa biểu tượng của Huế',
        description: 'Ngôi chùa cổ kính nhất xứ Huế bên bờ sông Hương. Tháp Phước Duyên 7 tầng là biểu tượng không thể nhầm lẫn.',
        category: 'culture', lat: 16.4533, lng: 107.5509, weather: 'cloudy', rating: 4.7, region: 'Huế' },
    { id: '3', title: 'Lăng Khải Định', subtitle: 'Lăng mộ triều Nguyễn',
        description: 'Lăng mộ độc đáo pha trộn kiến trúc Á-Âu, trang trí bằng hàng triệu mảnh sứ và kính màu tinh xảo.',
        category: 'history', lat: 16.3980, lng: 107.5966, weather: 'sunny', rating: 4.6, region: 'Huế' },
    { id: '4', title: 'Lăng Tự Đức', subtitle: 'Khu lăng tẩm thơ mộng',
        description: 'Một trong những lăng đẹp nhất, nơi vua Tự Đức an nghỉ giữa hồ sen và rừng thông tĩnh lặng.',
        category: 'history', lat: 16.4570, lng: 107.5530, weather: 'cloudy', rating: 4.5, region: 'Huế' },
    { id: '5', title: 'Bún bò Huế Mệ Tai', subtitle: 'Ẩm thực truyền thống xứ Huế',
        description: 'Quán bún bò nổi tiếng với công thức gia truyền hơn 40 năm. Nước dùng cay nồng đặc trưng.',
        category: 'food', lat: 16.4637, lng: 107.5953, weather: 'rainy', rating: 4.9, region: 'Huế' },
    { id: '6', title: 'Chợ Đông Ba', subtitle: 'Khu chợ sầm uất nhất Huế',
        description: 'Ngôi chợ truyền thống lớn nhất Huế, thiên đường ẩm thực và đặc sản với đủ món chè, mè xửng, tôm chua.',
        category: 'food', lat: 16.4708, lng: 107.5878, weather: 'cloudy', rating: 4.4, region: 'Huế' },
    { id: '7', title: 'Cầu Trường Tiền', subtitle: 'Biểu tượng sông Hương',
        description: 'Cây cầu thép cổ bắc qua sông Hương, lung linh sắc màu mỗi đêm và là biểu tượng lãng mạn của Huế.',
        category: 'culture', lat: 16.4690, lng: 107.5920, weather: 'sunny', rating: 4.6, region: 'Huế' },

    // ── Đà Nẵng ──
    { id: '8', title: 'Bà Nà Hills', subtitle: 'Cầu Vàng & Làng Pháp',
        description: 'Khu du lịch núi nổi tiếng với Cầu Vàng đỡ bằng đôi bàn tay khổng lồ. Khí hậu mát mẻ quanh năm.',
        category: 'amusement', lat: 15.9977, lng: 107.9966, weather: 'cloudy', rating: 4.8, region: 'Đà Nẵng' },
    { id: '9', title: 'Sun World Asia Park', subtitle: 'Công viên giải trí Đà Nẵng',
        description: 'Công viên châu Á với vòng quay Sun Wheel khổng lồ và nhiều trò chơi mạo hiểm hấp dẫn.',
        category: 'amusement', lat: 16.0382, lng: 108.2253, weather: 'sunny', rating: 4.5, region: 'Đà Nẵng' },
    { id: '10', title: 'Biển Mỹ Khê', subtitle: 'Top 6 bãi biển quyến rũ nhất hành tinh',
        description: 'Bãi biển trắng mịn, sóng hiền hòa, được Forbes bình chọn là một trong những bãi biển đẹp nhất thế giới.',
        category: 'nature', lat: 16.0576, lng: 108.2419, weather: 'sunny', rating: 4.6, region: 'Đà Nẵng' },
    { id: '11', title: 'Cầu Rồng', subtitle: 'Biểu tượng thành phố Đà Nẵng',
        description: 'Cây cầu hình rồng phun lửa và phun nước vào cuối tuần — công trình kiến trúc hiện đại biểu tượng.',
        category: 'culture', lat: 16.0614, lng: 108.2270, weather: 'sunny', rating: 4.5, region: 'Đà Nẵng' },
    { id: '12', title: 'Ngũ Hành Sơn', subtitle: 'Núi đá vôi linh thiêng',
        description: 'Quần thể năm ngọn núi đá vôi với hang động, chùa chiền và tầm nhìn toàn cảnh thành phố biển.',
        category: 'nature', lat: 16.0040, lng: 108.2630, weather: 'sunny', rating: 4.5, region: 'Đà Nẵng' },
    { id: '13', title: 'Chùa Linh Ứng Sơn Trà', subtitle: 'Tượng Phật Bà cao nhất Việt Nam',
        description: 'Ngôi chùa trên bán đảo Sơn Trà với tượng Quan Âm cao 67m, nhìn ra toàn cảnh biển Đà Nẵng.',
        category: 'culture', lat: 16.1006, lng: 108.2999, weather: 'cloudy', rating: 4.7, region: 'Đà Nẵng' },
    { id: '14', title: 'Mì Quảng Bà Mua', subtitle: 'Đặc sản xứ Quảng',
        description: 'Quán mì Quảng trứ danh với sợi mì vàng óng, tôm thịt đậm đà và bánh tráng giòn rụm.',
        category: 'food', lat: 16.0470, lng: 108.2100, weather: 'rainy', rating: 4.7, region: 'Đà Nẵng' },

    // ── Hội An / Quảng Nam ──
    { id: '15', title: 'Phố Cổ Hội An', subtitle: 'Di sản văn hóa thế giới UNESCO',
        description: 'Khu phố cổ được bảo tồn hoàn hảo nhất Đông Nam Á, lung linh đèn lồng đỏ mỗi đêm rằm.',
        category: 'history', lat: 15.8797, lng: 108.3351, weather: 'sunny', rating: 4.9, region: 'Hội An' },
    { id: '16', title: 'Chùa Cầu', subtitle: 'Biểu tượng Hội An',
        description: 'Cây cầu gỗ cổ do thương nhân Nhật Bản xây dựng thế kỷ 17, in trên tờ tiền 20.000 đồng.',
        category: 'history', lat: 15.8773, lng: 108.3265, weather: 'sunny', rating: 4.7, region: 'Hội An' },
    { id: '17', title: 'Bánh mì Phượng', subtitle: 'Bánh mì ngon nhất thế giới',
        description: 'Quán bánh mì huyền thoại được Anthony Bourdain ca ngợi. Hàng dài nối đuôi từ sáng tới tối.',
        category: 'food', lat: 15.8800, lng: 108.3348, weather: 'cloudy', rating: 4.8, region: 'Hội An' },
    { id: '18', title: 'Rừng dừa Bảy Mẫu', subtitle: 'Trải nghiệm thuyền thúng',
        description: 'Khu rừng dừa nước xanh mướt, nơi du khách ngồi thuyền thúng và xem múa thúng độc đáo.',
        category: 'nature', lat: 15.8939, lng: 108.3540, weather: 'cloudy', rating: 4.4, region: 'Hội An' },
    { id: '19', title: 'Biển An Bàng', subtitle: 'Bãi biển yên bình',
        description: 'Bãi biển hoang sơ, thư thái với hàng quán ven biển và hoàng hôn tuyệt đẹp.',
        category: 'nature', lat: 15.9069, lng: 108.3490, weather: 'sunny', rating: 4.5, region: 'Hội An' },
    { id: '20', title: 'Lễ hội đèn lồng Hội An', subtitle: 'Đêm rằm phố Hội',
        description: 'Mỗi đêm 14 âm lịch, cả phố cổ tắt điện, thắp đèn lồng và thả hoa đăng trên sông Hoài.',
        category: 'festival', lat: 15.8794, lng: 108.3300, weather: 'cloudy', rating: 4.9, region: 'Hội An' },
    { id: '21', title: 'VinWonders Nam Hội An', subtitle: 'Công viên chủ đề lớn',
        description: 'Tổ hợp vui chơi giải trí với công viên nước, vườn thú safari và khu tái hiện văn hóa dân gian.',
        category: 'amusement', lat: 15.7560, lng: 108.3470, weather: 'sunny', rating: 4.6, region: 'Quảng Nam' },
    { id: '22', title: 'Thánh địa Mỹ Sơn', subtitle: 'Đền tháp Chăm Pa cổ đại',
        description: 'Quần thể đền tháp Chăm Pa từ thế kỷ 4–13 giữa thung lũng xanh. Di sản văn hóa thế giới UNESCO.',
        category: 'history', lat: 15.7625, lng: 108.1292, weather: 'rainy', rating: 4.5, region: 'Quảng Nam' },
];

const CITY_HUBS: CityHub[] = [
    { id: 'hue', name: 'Huế', lat: 16.4637, lng: 107.5909, count: 7 },
    { id: 'danang', name: 'Đà Nẵng', lat: 16.0544, lng: 108.2022, count: 7 },
    { id: 'hoian', name: 'Hội An', lat: 15.8794, lng: 108.3350, count: 6 },
    { id: 'quangnam', name: 'Quảng Nam', lat: 15.7625, lng: 108.1292, count: 2 },
];

const GOONG_API_KEY = process.env.NEXT_PUBLIC_GOONG_API_KEY ?? '';

async function loadGoongSdk() {
    try {
        // The package entry can fail under Next dev bundlers; the browser dist
        // bundle is closer to how Goong expects to run in a client-only map.
        const mod = await import('@goongmaps/goong-js/dist/goong-js');
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return ((mod as any).default ?? mod) as any;
    } catch {
        const mod = await import('@goongmaps/goong-js');
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return ((mod as any).default ?? mod) as any;
    }
}

function canUseWebGL(): boolean {
    if (typeof window === 'undefined') return false;

    try {
        const canvas = document.createElement('canvas');
        const context = canvas.getContext('webgl2') ?? canvas.getContext('webgl') ?? canvas.getContext('experimental-webgl');
        return Boolean(context);
    } catch {
        return false;
    }
}

function getPlaceWeather(
    place: Place,
    weatherByPlace: Record<string, ApiWeatherCondition>,
): WeatherCondition {
    const liveWeather = weatherByPlace[place.id];
    if (!liveWeather || liveWeather === 'any') return place.weather;
    if (liveWeather === 'hot') return 'sunny';
    return liveWeather;
}

function hasLiveWeather(
    place: Place,
    weatherByPlace: Record<string, ApiWeatherCondition>,
): boolean {
    const liveWeather = weatherByPlace[place.id];
    return Boolean(liveWeather && liveWeather !== 'any');
}

function isFeaturedPlace(place: Place): boolean {
    return place.rating >= 4.7 || place.category === 'history' || place.category === 'culture';
}

function cityFeatureCollection() {
    return {
        type: 'FeatureCollection',
        features: CITY_HUBS.map((city) => ({
            type: 'Feature',
            properties: {
                id: city.id,
                title: city.name,
                count: city.count,
            },
            geometry: {
                type: 'Point',
                coordinates: [city.lng, city.lat],
            },
        })),
    };
}

function placeFeatureCollection(
    places: Place[],
    selectedPlace: Place | null,
    weatherByPlace: Record<string, ApiWeatherCondition>,
) {
    return {
        type: 'FeatureCollection',
        features: places.map((place) => {
            const weather = getPlaceWeather(place, weatherByPlace);
            return {
                type: 'Feature',
                properties: {
                    id: place.id,
                    title: place.title,
                    subtitle: place.subtitle,
                    category: place.category,
                    categoryLabel: CATEGORY_LABEL[place.category],
                    categoryShort: CATEGORY_SHORT[place.category],
                    region: place.region,
                    rating: place.rating,
                    weather,
                    selected: selectedPlace?.id === place.id,
                },
                geometry: {
                    type: 'Point',
                    coordinates: [place.lng, place.lat],
                },
            };
        }),
    };
}

function nearbyFeatureCollection(items: NearbyPlace[]) {
    return {
        type: 'FeatureCollection',
        features: items.map((item) => ({
            type: 'Feature',
            properties: {
                id: item.id,
                name: item.name,
                category: item.category,
                color: CATEGORY_COLORS[item.category as Category] ?? CATEGORY_COLORS.place,
                label: item.category_label,
            },
            geometry: {
                type: 'Point',
                coordinates: [item.longitude, item.latitude],
            },
        })),
    };
}

function haversineMeters(lat1: number, lon1: number, lat2: number, lon2: number): number {
    const R = 6_371_000;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
    return 2 * R * Math.asin(Math.sqrt(a));
}

function setSourceData(map: ExploreMap, sourceId: string, data: unknown) {
    const source = map.getSource?.(sourceId);
    source?.setData?.(data);
}

function upsertMapSource(map: ExploreMap, sourceId: string, data: unknown) {
    if (map.getSource?.(sourceId)) {
        setSourceData(map, sourceId, data);
        return;
    }
    map.addSource?.(sourceId, {
        type: 'geojson',
        data,
    });
}

function addLayerOnce(map: ExploreMap, layer: ExploreLayerSpec) {
    if (!map.getLayer?.(layer.id)) {
        map.addLayer?.(layer);
    }
}

function addExploreLayers(map: ExploreMap) {
    upsertMapSource(map, 'bv-cities', cityFeatureCollection());
    upsertMapSource(map, 'bv-featured-places', placeFeatureCollection([], null, {}));
    upsertMapSource(map, 'bv-detail-places', placeFeatureCollection([], null, {}));
    upsertMapSource(map, 'bv-nearby', nearbyFeatureCollection([]));

    addLayerOnce(map, {
        id: 'bv-city-hub-glow',
        type: 'circle',
        source: 'bv-cities',
        maxzoom: 8.7,
        paint: {
            'circle-radius': ['interpolate', ['linear'], ['zoom'], 5, 18, 8.7, 36],
            'circle-color': 'rgba(198, 154, 63, 0.18)',
            'circle-stroke-color': '#c69a3f',
            'circle-stroke-width': 1.5,
            'circle-blur': 0.2,
        },
    });

    addLayerOnce(map, {
        id: 'bv-city-hub-dot',
        type: 'circle',
        source: 'bv-cities',
        maxzoom: 8.7,
        paint: {
            'circle-radius': ['interpolate', ['linear'], ['zoom'], 5, 5, 8.7, 9],
            'circle-color': '#c69a3f',
            'circle-stroke-color': '#1a120b',
            'circle-stroke-width': 2,
        },
    });

    addLayerOnce(map, {
        id: 'bv-city-hub-label',
        type: 'symbol',
        source: 'bv-cities',
        maxzoom: 8.7,
        layout: {
            'text-field': ['format', ['get', 'title'], { 'font-scale': 1.1 }, '\n', {}, ['concat', ['to-string', ['get', 'count']], ' điểm'], { 'font-scale': 0.72 }],
            'text-size': ['interpolate', ['linear'], ['zoom'], 5, 13, 8.7, 18],
            'text-offset': [0, 1.6],
            'text-anchor': 'top',
            'text-allow-overlap': false,
            'text-ignore-placement': false,
        },
        paint: {
            'text-color': '#efe6d2',
            'text-halo-color': '#1a120b',
            'text-halo-width': 1.6,
        },
    });

    const categoryColor = [
        'match',
        ['get', 'category'],
        'history', CATEGORY_COLORS.history,
        'culture', CATEGORY_COLORS.culture,
        'food', CATEGORY_COLORS.food,
        'nature', CATEGORY_COLORS.nature,
        'festival', CATEGORY_COLORS.festival,
        'amusement', CATEGORY_COLORS.amusement,
        '#c69a3f',
    ];

    const weatherBubbleRadius = [
        'match',
        ['get', 'weather'],
        'sunny', 34,
        'cloudy', 25,
        'rainy', 17,
        22,
    ];

    const placeSources = [
        { source: 'bv-featured-places', prefix: 'featured', minzoom: 8.4, maxzoom: 11.5, labelSize: 12 },
        { source: 'bv-detail-places', prefix: 'detail', minzoom: 11, labelSize: 12 },
    ];

    placeSources.forEach(({ source, prefix, minzoom, maxzoom, labelSize }) => {
        addLayerOnce(map, {
            id: `bv-${prefix}-place-bubble`,
            type: 'circle',
            source,
            minzoom,
            ...(maxzoom ? { maxzoom } : {}),
            paint: {
                'circle-radius': [
                    'interpolate',
                    ['linear'],
                    ['zoom'],
                    minzoom,
                    ['*', weatherBubbleRadius, 0.55],
                    14,
                    weatherBubbleRadius,
                ],
                'circle-color': [
                    'match',
                    ['get', 'weather'],
                    'sunny', 'rgba(251, 191, 36, 0.22)',
                    'cloudy', 'rgba(96, 165, 250, 0.18)',
                    'rainy', 'rgba(167, 139, 250, 0.18)',
                    'rgba(198, 154, 63, 0.18)',
                ],
                'circle-stroke-color': [
                    'match',
                    ['get', 'weather'],
                    'sunny', '#fbbf24',
                    'cloudy', '#60a5fa',
                    'rainy', '#a78bfa',
                    '#c69a3f',
                ],
                'circle-stroke-width': ['case', ['get', 'selected'], 2.5, 1.2],
                'circle-blur': 0.15,
            },
        });

        addLayerOnce(map, {
            id: `bv-${prefix}-place-pin`,
            type: 'circle',
            source,
            minzoom,
            ...(maxzoom ? { maxzoom } : {}),
            paint: {
                'circle-radius': ['case', ['get', 'selected'], 9, 7],
                'circle-color': categoryColor,
                'circle-stroke-color': '#fffaf0',
                'circle-stroke-width': ['case', ['get', 'selected'], 3, 2],
            },
        });

        addLayerOnce(map, {
            id: `bv-${prefix}-place-short`,
            type: 'symbol',
            source,
            minzoom,
            ...(maxzoom ? { maxzoom } : {}),
            layout: {
                'text-field': ['get', 'categoryShort'],
                'text-size': 9,
                'text-allow-overlap': true,
                'text-ignore-placement': true,
            },
            paint: {
                'text-color': '#ffffff',
            },
        });

        addLayerOnce(map, {
            id: `bv-${prefix}-place-label`,
            type: 'symbol',
            source,
            minzoom: minzoom + 0.4,
            ...(maxzoom ? { maxzoom } : {}),
            layout: {
                'text-field': ['get', 'title'],
                'text-size': ['interpolate', ['linear'], ['zoom'], minzoom, labelSize, 14, labelSize + 2],
                'text-offset': [0, 1.35],
                'text-anchor': 'top',
                'text-allow-overlap': false,
                'text-ignore-placement': false,
            },
            paint: {
                'text-color': '#efe6d2',
                'text-halo-color': '#1a120b',
                'text-halo-width': 1.4,
            },
        });
    });

    // Live Foursquare POIs (cafe / homestay / restaurant ...). Refreshed on moveend.
    addLayerOnce(map, {
        id: 'bv-nearby-pin',
        type: 'circle',
        source: 'bv-nearby',
        minzoom: NEARBY_MIN_ZOOM,
        paint: {
            'circle-radius': ['interpolate', ['linear'], ['zoom'], NEARBY_MIN_ZOOM, 4, 17, 6.5],
            'circle-color': ['get', 'color'],
            'circle-stroke-color': '#1a120b',
            'circle-stroke-width': 1.4,
            'circle-opacity': 0.9,
        },
    });

    addLayerOnce(map, {
        id: 'bv-nearby-label',
        type: 'symbol',
        source: 'bv-nearby',
        minzoom: NEARBY_MIN_ZOOM + 1,
        layout: {
            'text-field': ['get', 'name'],
            'text-size': 10,
            'text-offset': [0, 1.1],
            'text-anchor': 'top',
            'text-allow-overlap': false,
            'text-optional': true,
        },
        paint: {
            'text-color': '#d9cdb4',
            'text-halo-color': '#1a120b',
            'text-halo-width': 1.2,
        },
    });
}

// Frame the map to show every POI on first load.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function fitToPlaces(map: any) {
    try {
        const lngs = MOCK_PLACES.map((p) => p.lng);
        const lats = MOCK_PLACES.map((p) => p.lat);
        map.fitBounds(
            [
                [Math.min(...lngs), Math.min(...lats)],
                [Math.max(...lngs), Math.max(...lats)],
            ],
            { padding: 90, duration: 0 },
        );
    } catch { /* ignore */ }
}

// Hide the base-map's built-in POI symbols (broken/cluttered in dark style).
// We render our own clean markers instead.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function hideBasePoiIcons(map: any) {
    try {
        const style = map.getStyle?.();
        if (!style?.layers) return;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        style.layers.forEach((layer: any) => {
            const id = (layer.id || '').toLowerCase();
            const srcLayer = (layer['source-layer'] || '').toLowerCase();
            if (layer.type === 'symbol' && (id.includes('poi') || srcLayer.includes('poi'))) {
                try { map.setLayoutProperty(layer.id, 'visibility', 'none'); } catch { /* ignore */ }
            }
        });
    } catch { /* ignore */ }
}

// ─── Component ────────────────────────────────────────────────────────────────

export function ExplorePage() {
    const mapContainerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<Map | null>(null);
    const currentMarkerRef = useRef<Marker | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const goongRef = useRef<any>(null);

    const [mapLoaded, setMapLoaded] = useState(false);
    const [selectedPlace, setSelectedPlace] = useState<Place | null>(null);
    const [activeCategory, setActiveCategory] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [locating, setLocating] = useState(false);
    const [locationError, setLocationError] = useState('');
    const [mapError, setMapError] = useState('');
    const [weatherByPlace, setWeatherByPlace] = useState<Record<string, ApiWeatherCondition>>({});
    const [nearbyItems, setNearbyItems] = useState<NearbyPlace[]>([]);

    // Lock body scroll for full-screen map
    useEffect(() => {
        const previousHtmlOverflow = document.documentElement.style.overflow;
        const previousBodyOverflow = document.body.style.overflow;
        document.documentElement.style.overflow = 'hidden';
        document.body.style.overflow = 'hidden';
        return () => {
            document.documentElement.style.overflow = previousHtmlOverflow;
            document.body.style.overflow = previousBodyOverflow;
        };
    }, []);

    useEffect(() => {
        if (typeof window !== 'undefined' && window.innerWidth > 768) {
            const frame = window.requestAnimationFrame(() => setSidebarOpen(true));
            return () => window.cancelAnimationFrame(frame);
        }
    }, []);

    // Initialize Goong map — loaded dynamically to avoid SSR issues
    useEffect(() => {
        const container = mapContainerRef.current;
        if (!container || !GOONG_API_KEY || mapRef.current) return;

        let map: Map | null = null;
        let cancelled = false;
        let hasLoaded = false;

        const initMap = async () => {
            try {
                setMapError('');
                const G = await loadGoongSdk();
                if (cancelled) return;

                if (!G?.Map || !G?.Marker) {
                    throw new Error('Goong JS SDK did not expose Map/Marker constructors.');
                }

                if (!canUseWebGL() || (typeof G.supported === 'function' && !G.supported())) {
                    throw new Error('WEBGL_UNSUPPORTED');
                }

                // Goong JS requires accessToken to be set before creating Map
                G.accessToken = GOONG_API_KEY;
                goongRef.current = G;

                map = new G.Map({
                    container,
                    style: `https://tiles.goong.io/assets/goong_map_dark.json?api_key=${GOONG_API_KEY}`,
                    center: [107.95, 16.15],
                    zoom: 8,
                }) as Map;

                map.on('error', () => {
                    if (!cancelled && !hasLoaded) {
                        setMapError('Không thể tải bản đồ Goong lúc này. Danh sách địa điểm vẫn có thể dùng được.');
                    }
                });

                if (G.NavigationControl) {
                    map.addControl(new G.NavigationControl({ showCompass: false }), 'bottom-right');
                }
                mapRef.current = map;

                map.on('load', () => {
                    if (cancelled) return;
                    hasLoaded = true;
                    hideBasePoiIcons(map);
                    addExploreLayers(map as ExploreMap);
                    fitToPlaces(map);
                    setMapLoaded(true);
                    setMapError('');
                });
            } catch (error) {
                if (cancelled) return;
                setMapLoaded(false);
                const message = error instanceof Error && error.message === 'WEBGL_UNSUPPORTED'
                    ? 'Trình duyệt hoặc máy hiện tại chưa hỗ trợ WebGL ổn định. Danh sách địa điểm vẫn có thể dùng được.'
                    : 'Không thể khởi tạo bản đồ Goong. Danh sách địa điểm vẫn có thể dùng được.';
                setMapError(message);
                mapRef.current = null;
                goongRef.current = null;
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                (map as any)?.remove?.();
            }
        };

        initMap();

        return () => {
            cancelled = true;
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (map as any)?.remove();
            mapRef.current = null;
            setMapLoaded(false);
        };
    }, []);

    // Keep the GL canvas in sync with its container. The sidebar is an in-flow
    // grid column, so opening/closing it changes the map width — without this
    // the canvas keeps its old size and the map looks stretched/deformed.
    useEffect(() => {
        const container = mapContainerRef.current;
        if (!container || typeof ResizeObserver === 'undefined') return;
        const observer = new ResizeObserver(() => {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (mapRef.current as any)?.resize?.();
        });
        observer.observe(container);
        return () => observer.disconnect();
    }, []);

    // Live POIs (Foursquare via backend), fetched for the current viewport and
    // refreshed whenever the map stops moving — the "real-time" marker feel.
    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const m = mapRef.current as any;
        let cancelled = false;
        let timer: ReturnType<typeof setTimeout> | undefined;

        const fetchNearby = async () => {
            if (cancelled) return;
            if ((m.getZoom?.() ?? 0) < NEARBY_MIN_ZOOM) {
                setNearbyItems([]);
                return;
            }
            const center = m.getCenter?.();
            const bounds = m.getBounds?.();
            const ne = bounds?.getNorthEast?.();
            if (!center) return;
            const radius = ne
                ? Math.min(50000, Math.max(300, Math.round(haversineMeters(center.lat, center.lng, ne.lat, ne.lng))))
                : 2000;
            const res = await nearbyApi.search(center.lat, center.lng, radius, 40);
            if (cancelled || !res.data) return;
            setNearbyItems(res.data.items);
        };

        const onMoveEnd = () => {
            if (timer) clearTimeout(timer);
            timer = setTimeout(fetchNearby, 400);
        };

        m.on?.('moveend', onMoveEnd);
        fetchNearby();

        return () => {
            cancelled = true;
            if (timer) clearTimeout(timer);
            m.off?.('moveend', onMoveEnd);
        };
    }, [mapLoaded]);

    // Render nearby POIs, filtered by the active category chip.
    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;
        const visible = activeCategory === 'all'
            ? nearbyItems
            : nearbyItems.filter((p) => p.category === activeCategory);
        setSourceData(mapRef.current, 'bv-nearby', nearbyFeatureCollection(visible));
    }, [nearbyItems, activeCategory, mapLoaded]);

    const filteredPlaces = useMemo(() =>
        MOCK_PLACES.filter((p) => {
            const matchCat = activeCategory === 'all' || p.category === activeCategory;
            const q = searchQuery.toLowerCase();
            const matchSearch = q === '' ||
                p.title.toLowerCase().includes(q) ||
                p.region.toLowerCase().includes(q) ||
                p.subtitle.toLowerCase().includes(q);
            return matchCat && matchSearch;
        }),
        [activeCategory, searchQuery],
    );

    const handleSelectPlace = useCallback((place: Place) => {
        setSelectedPlace(place);
        mapRef.current?.flyTo({ center: [place.lng, place.lat], zoom: 13, speed: 1.4 });
    }, []);

    useEffect(() => {
        if (!mapLoaded) return;

        let cancelled = false;

        const loadWeather = async () => {
            const response = await weatherApi.getBatch(
                MOCK_PLACES.map((place) => ({ lat: place.lat, lng: place.lng })),
            );
            if (cancelled || !response.data) return;

            const nextWeatherByPlace: Record<string, ApiWeatherCondition> = {};
            response.data.results.forEach((result, index) => {
                const place = MOCK_PLACES[index];
                if (place) nextWeatherByPlace[place.id] = result.condition;
            });
            setWeatherByPlace(nextWeatherByPlace);
        };

        loadWeather();

        return () => {
            cancelled = true;
        };
    }, [mapLoaded]);

    // Locate the user and drop a "you are here" marker
    const handleLocate = useCallback(() => {
        if (!navigator.geolocation || !mapRef.current || !goongRef.current) return;
        setLocating(true);
        setLocationError('');
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const { longitude, latitude } = pos.coords;
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const G = goongRef.current as any;
                currentMarkerRef.current?.remove();
                const el = document.createElement('div');
                el.className = 'bv-current-dot';
                currentMarkerRef.current = new G.Marker({ element: el })
                    .setLngLat([longitude, latitude])
                    .addTo(mapRef.current!);
                mapRef.current!.flyTo({ center: [longitude, latitude], zoom: 14, speed: 1.6 });
                setLocating(false);
            },
            () => {
                setLocating(false);
                setLocationError('Không thể lấy vị trí. Kiểm tra quyền truy cập vị trí trong trình duyệt.');
            },
            { enableHighAccuracy: true, timeout: 8000 },
        );
    }, []);

    // Update map-engine layers whenever filtered places or selection changes.
    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;

        const featuredPlaces = filteredPlaces.filter(isFeaturedPlace);
        setSourceData(mapRef.current, 'bv-featured-places', placeFeatureCollection(featuredPlaces, selectedPlace, weatherByPlace));
        setSourceData(mapRef.current, 'bv-detail-places', placeFeatureCollection(filteredPlaces, selectedPlace, weatherByPlace));
    }, [mapLoaded, filteredPlaces, selectedPlace, weatherByPlace]);

    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;

        const map = mapRef.current as ExploreInteractiveMap;
        const layerIds = [
            'bv-featured-place-bubble',
            'bv-featured-place-pin',
            'bv-featured-place-short',
            'bv-featured-place-label',
            'bv-detail-place-bubble',
            'bv-detail-place-pin',
            'bv-detail-place-short',
            'bv-detail-place-label',
        ];

        const handleClick = (event: ExploreLayerClickEvent) => {
            const id = event.features?.[0]?.properties?.id;
            const place = MOCK_PLACES.find((item) => item.id === id);
            if (place) handleSelectPlace(place);
        };

        const setPointer = () => {
            const canvas = map.getCanvas?.();
            if (canvas) canvas.style.cursor = 'pointer';
        };
        const unsetPointer = () => {
            const canvas = map.getCanvas?.();
            if (canvas) canvas.style.cursor = '';
        };

        layerIds.forEach((layerId) => {
            if (!map.getLayer?.(layerId)) return;
            map.on('click', layerId, handleClick);
            map.on('mouseenter', layerId, setPointer);
            map.on('mouseleave', layerId, unsetPointer);
        });

        return () => {
            layerIds.forEach((layerId) => {
                if (!map.getLayer?.(layerId)) return;
                map.off('click', layerId, handleClick);
                map.off('mouseenter', layerId, setPointer);
                map.off('mouseleave', layerId, unsetPointer);
            });
        };
    }, [mapLoaded, handleSelectPlace]);

    const selectedWeather = selectedPlace ? getPlaceWeather(selectedPlace, weatherByPlace) : null;
    const selectedHasLiveWeather = selectedPlace ? hasLiveWeather(selectedPlace, weatherByPlace) : false;
    const weatherBadgeClass = selectedWeather
        ? styles[`badge${selectedWeather.charAt(0).toUpperCase() + selectedWeather.slice(1)}` as keyof typeof styles]
        : '';

    return (
        <div className={styles.mapPage}>
            <nav className={styles.mapRail} aria-label="Explore map controls">
                <button
                    className={styles.railButton}
                    onClick={() => setSidebarOpen((open) => !open)}
                    aria-label={sidebarOpen ? 'Ẩn danh sách địa điểm' : 'Mở danh sách địa điểm'}
                    aria-pressed={sidebarOpen}
                >
                    <List weight="bold" />
                </button>
                <div className={styles.railDivider} />
                <button
                    className={`${styles.railTextButton} ${sidebarOpen ? styles.railTextButtonActive : ''}`}
                    onClick={() => setSidebarOpen(true)}
                >
                    Explore
                </button>
                <button className={styles.railTextButton}>Saved</button>
            </nav>

            {/* Mobile overlay */}
            {sidebarOpen && (
                <div
                    className={styles.sidebarOverlay}
                    onClick={() => setSidebarOpen(false)}
                />
            )}

            {/* ── Sidebar ── */}
            <aside className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarOpen : styles.sidebarCollapsed}`}>
                <div className={styles.sidebarHeader}>
                    <div className={styles.sidebarBrand}>
                        <span className={styles.sidebarTitle}>Khám phá gần bạn</span>
                        <button
                            className={styles.panelClose}
                            onClick={() => setSidebarOpen(false)}
                            aria-label="Ẩn danh sách địa điểm"
                        >
                            <X />
                        </button>
                    </div>
                    <div className={styles.sidebarSubtitle}>
                        {filteredPlaces.length} địa điểm văn hóa, ăn uống và trải nghiệm.
                    </div>
                </div>

                {/* Search */}
                <div className={styles.searchContainer}>
                    <div className={styles.searchInputWrapper}>
                        <span className={styles.searchIcon}><MagnifyingGlass /></span>
                        <input
                            className={styles.searchInput}
                            type="text"
                            placeholder="Tìm địa điểm, vùng..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                        {searchQuery && (
                            <button
                                className={styles.searchClear}
                                onClick={() => setSearchQuery('')}
                                aria-label="Xóa tìm kiếm"
                            >
                                <X />
                            </button>
                        )}
                    </div>
                </div>

                {/* Category filters */}
                <div className={styles.filtersContainer}>
                    <div className={styles.filtersList}>
                        {FILTER_CHIPS.map((chip) => (
                            <button
                                key={chip.key}
                                className={`${styles.filterChip} ${activeCategory === chip.key ? styles.filterChipActive : ''}`}
                                onClick={() => setActiveCategory(chip.key)}
                            >
                                {chip.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Places list */}
                <div className={styles.placesList}>
                    {filteredPlaces.map((place) => {
                        const placeWeather = getPlaceWeather(place, weatherByPlace);
                        const placeHasLiveWeather = hasLiveWeather(place, weatherByPlace);
                        return (
                            <button
                                key={place.id}
                                className={`${styles.placeItem} ${selectedPlace?.id === place.id ? styles.placeItemSelected : ''}`}
                                onClick={() => handleSelectPlace(place)}
                            >
                                <div className={styles.placeInfo}>
                                    <div className={styles.placeName}>{place.title}</div>
                                    <div className={styles.placeRegion}>{place.region} · {CATEGORY_LABEL[place.category]}</div>
                                    <div className={styles.placeHint}>{place.subtitle}</div>
                                </div>
                                <div className={styles.placeRightMeta}>
                                    <span className={styles.placeRating}><Star weight="fill" /> {place.rating}</span>
                                    <span className={styles.placeWeatherText}>
                                        {WEATHER_LABELS[placeWeather]}{placeHasLiveWeather ? ' · live' : ''}
                                    </span>
                                </div>
                            </button>
                        );
                    })}
                </div>

                {/* Weather legend */}
                <div className={styles.weatherLegend}>
                    <div className={styles.weatherLegendTitle}>Bubble size</div>
                    <div className={styles.weatherScale}>
                        <span>Vắng</span>
                        <div className={styles.weatherScaleTrack}>
                            <i />
                            <i />
                            <i />
                        </div>
                        <span>Đông</span>
                    </div>
                </div>
            </aside>

            {/* ── Map Area ── */}
            <div className={styles.mapArea}>
                {/* Map container */}
                <div ref={mapContainerRef} style={{ width: '100%', height: '100%' }} />

                {/* Locate-me button */}
                {GOONG_API_KEY && mapLoaded && (
                    <button
                        className={`${styles.locateBtn} ${locating ? styles.locateBtnActive : ''}`}
                        onClick={handleLocate}
                        title="Vị trí của tôi"
                        aria-label="Vị trí của tôi"
                    >
                        <span className={locating ? styles.locateBtnSpinning : ''}>
                            <Crosshair weight="bold" />
                        </span>
                    </button>
                )}

                {!sidebarOpen && (
                    <button
                        className={styles.closedSearch}
                        onClick={() => setSidebarOpen(true)}
                    >
                        <MagnifyingGlass />
                        <span>Tìm địa điểm, vùng...</span>
                    </button>
                )}

                {locationError && (
                    <div className={styles.mapNotice}>
                        {locationError}
                        <button onClick={() => setLocationError('')} aria-label="Đóng thông báo">
                            <X />
                        </button>
                    </div>
                )}

                {/* Overlay: API key missing */}
                {!GOONG_API_KEY && (
                    <div className={styles.mapStatus}>
                        <div className={styles.mapStatusTitle}>Goong Maps chưa được cấu hình</div>
                        <div className={styles.mapStatusDesc}>
                            Thêm Goong API key vào file <code>.env</code> để hiển thị bản đồ tương tác.
                        </div>
                        <code className={styles.mapStatusCode}>
                            NEXT_PUBLIC_GOONG_API_KEY=your_key_here
                        </code>
                        <div className={styles.mapStatusDesc} style={{ fontSize: '0.72rem', marginTop: 4 }}>
                            Đăng ký miễn phí tại account.goong.io
                        </div>
                    </div>
                )}

                {/* Overlay: Map fallback */}
                {GOONG_API_KEY && mapError && (
                    <div className={styles.mapStatus}>
                        <div className={styles.mapStatusTitle}>Bản đồ chưa sẵn sàng</div>
                        <div className={styles.mapStatusDesc}>{mapError}</div>
                    </div>
                )}

                {/* Overlay: Map loading */}
                {GOONG_API_KEY && !mapLoaded && !mapError && (
                    <div className={styles.mapStatus}>
                        <div className={styles.mapLoadingSpinner} />
                        <div className={styles.mapStatusDesc}>Đang tải bản đồ...</div>
                    </div>
                )}

                {/* Place detail card */}
                <div className={`${styles.detailCard} ${selectedPlace ? styles.detailCardVisible : ''}`}>
                    {selectedPlace && (
                        <>
                            <div className={styles.detailCardTop}>
                                <div className={styles.detailCardMeta}>
                                    <div className={styles.detailCardTitle}>{selectedPlace.title}</div>
                                    <div className={styles.detailCardSubtitle}>{selectedPlace.subtitle}</div>
                                </div>
                                <button
                                    className={styles.detailCardClose}
                                    onClick={() => setSelectedPlace(null)}
                                    aria-label="Đóng"
                                >
                                    <X />
                                </button>
                            </div>

                            <div className={styles.detailCardBadges}>
                                <span className={`${styles.badge} ${styles.badgeCategory}`}>
                                    {CATEGORY_LABEL[selectedPlace.category]}
                                </span>
                                {selectedWeather && (
                                    <span className={`${styles.badge} ${weatherBadgeClass}`}>
                                        {WEATHER_LABELS[selectedWeather]}
                                    </span>
                                )}
                                {selectedHasLiveWeather && (
                                    <span className={`${styles.badge} ${styles.badgeLive}`}>
                                        live
                                    </span>
                                )}
                                <span className={styles.detailCardRating}>
                                    <Star weight="fill" /> {selectedPlace.rating}
                                </span>
                            </div>

                            <p className={styles.detailCardDesc}>{selectedPlace.description}</p>

                            <div className={styles.detailCardActions}>
                                <button className={styles.detailCardBtnPrimary}>
                                    Bắt đầu quest
                                </button>
                                <button
                                    className={styles.detailCardBtnSecondary}
                                    onClick={() => setSelectedPlace(null)}
                                >
                                    Đóng
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
