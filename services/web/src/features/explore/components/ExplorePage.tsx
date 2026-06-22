'use client';

import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import type { Map, Marker } from '@goongmaps/goong-js';
import { Crosshair, List, MagnifyingGlass, X } from '@phosphor-icons/react';
import styles from '../styles/explore.module.css';
import { weatherApi, nearbyApi, type WeatherCondition as ApiWeatherCondition, type NearbyPlace } from '@/lib/api';

// ─── Types ────────────────────────────────────────────────────────────────────

type WeatherCondition = 'sunny' | 'cloudy' | 'rainy';
type Category = 'history' | 'culture' | 'food' | 'nature' | 'festival' | 'amusement' | 'lodging' | 'place';


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

// Scan radius around the user's actual position. We only surface places within
// walking/short-ride distance of where the user is, not the whole viewport.
const NEARBY_RADIUS_METERS = 3000;

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

// Normalize the API weather condition to our 3-state bubble tint.
function normalizeWeather(condition: ApiWeatherCondition | null | undefined): WeatherCondition {
    if (condition === 'rainy') return 'rainy';
    if (condition === 'cloudy') return 'cloudy';
    return 'sunny';
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

type LatLng = { lat: number; lng: number };

// Effective distance used for sizing/labels: from the user's GPS when known,
// else from the Foursquare search point (map center).
function effectiveDistance(item: NearbyPlace, user: LatLng | null): number | null {
    if (user) return haversineMeters(user.lat, user.lng, item.latitude, item.longitude);
    return item.distance_meters;
}

// Dynamic bubble size from an external signal. Free tier: distance (closer =
// bigger). Premium: swap in popularity/rating. Returns 0..1.
function sizeWeight(item: NearbyPlace, distanceMeters: number | null): number {
    if (item.popularity != null) return Math.max(0, Math.min(1, item.popularity));
    if (item.rating != null) return Math.max(0, Math.min(1, item.rating / 10));
    const d = distanceMeters ?? 1500;
    // 0 m -> 1.0, 2000 m+ -> ~0.15
    return Math.max(0.15, Math.min(1, 1 - d / 2400));
}

function nearbyFeatureCollection(
    items: NearbyPlace[],
    selectedId: string | null,
    weather: WeatherCondition,
    user: LatLng | null,
) {
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
                weather,
                sizeWeight: sizeWeight(item, effectiveDistance(item, user)),
                selected: item.id === selectedId,
            },
            geometry: {
                type: 'Point',
                coordinates: [item.longitude, item.latitude],
            },
        })),
    };
}

function formatDistance(m: number): string {
    return m < 1000 ? `${Math.round(m)} m` : `${(m / 1000).toFixed(1)} km`;
}

// UV index -> WHO risk band (Vietnamese).
function uvLabel(uvi: number): string {
    if (uvi < 3) return 'Thấp';
    if (uvi < 6) return 'Trung bình';
    if (uvi < 8) return 'Cao';
    if (uvi < 11) return 'Rất cao';
    return 'Cực cao';
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
    upsertMapSource(map, 'bv-nearby', nearbyFeatureCollection([], null, 'sunny', null));

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

    // Live Foursquare POIs as dynamic bubbles. Size = sizeWeight (distance now,
    // popularity when Premium); tint = area weather; pin colour = category.
    addLayerOnce(map, {
        id: 'bv-nearby-bubble',
        type: 'circle',
        source: 'bv-nearby',
        minzoom: NEARBY_MIN_ZOOM,
        paint: {
            'circle-radius': [
                'interpolate', ['linear'], ['zoom'],
                NEARBY_MIN_ZOOM, ['*', ['get', 'sizeWeight'], 16],
                17, ['*', ['get', 'sizeWeight'], 40],
            ],
            'circle-color': [
                'match', ['get', 'weather'],
                'sunny', 'rgba(251, 191, 36, 0.22)',
                'cloudy', 'rgba(96, 165, 250, 0.18)',
                'rainy', 'rgba(167, 139, 250, 0.18)',
                'rgba(198, 154, 63, 0.18)',
            ],
            'circle-stroke-color': [
                'match', ['get', 'weather'],
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
        id: 'bv-nearby-pin',
        type: 'circle',
        source: 'bv-nearby',
        minzoom: NEARBY_MIN_ZOOM,
        paint: {
            'circle-radius': ['case', ['get', 'selected'], 8, 6],
            'circle-color': categoryColor,
            'circle-stroke-color': '#fffaf0',
            'circle-stroke-width': ['case', ['get', 'selected'], 3, 2],
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
            'text-offset': [0, 1.2],
            'text-anchor': 'top',
            'text-allow-overlap': false,
            'text-optional': true,
        },
        paint: {
            'text-color': '#ece0c6',
            'text-halo-color': '#15100a',
            'text-halo-width': 1.3,
        },
    });
}

// Keep the base map's built-in POIs (cà phê, quán ăn, siêu thị, ...) visible so
// the map feels dense like Google Maps, and make their labels legible on the
// dark lacquer style. The icons come from the Goong tiles; we only restyle text.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function enhanceBasePoiLabels(map: any) {
    try {
        const style = map.getStyle?.();
        if (!style?.layers) return;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        style.layers.forEach((layer: any) => {
            const id = (layer.id || '').toLowerCase();
            const srcLayer = (layer['source-layer'] || '').toLowerCase();
            if (layer.type !== 'symbol' || !(id.includes('poi') || srcLayer.includes('poi'))) return;
            const set = (fn: () => void) => { try { fn(); } catch { /* layer lacks this prop */ } };
            set(() => map.setLayoutProperty(layer.id, 'visibility', 'visible'));
            set(() => map.setPaintProperty(layer.id, 'text-color', '#ece0c6'));
            set(() => map.setPaintProperty(layer.id, 'text-halo-color', '#15100a'));
            set(() => map.setPaintProperty(layer.id, 'text-halo-width', 1.3));
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
    const [selectedPlace, setSelectedPlace] = useState<NearbyPlace | null>(null);
    const [activeCategory, setActiveCategory] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [locating, setLocating] = useState(false);
    const [locationError, setLocationError] = useState('');
    const [mapError, setMapError] = useState('');
    const [areaWeather, setAreaWeather] = useState<WeatherCondition>('sunny');
    const [areaInfo, setAreaInfo] = useState<{ temp: number | null; uvi: number | null; rain_mm: number | null } | null>(null);
    const [nearbyItems, setNearbyItems] = useState<NearbyPlace[]>([]);
    const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null);

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
                    enhanceBasePoiLabels(map);
                    addExploreLayers(map as ExploreMap);
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

    // Live POIs (Foursquare via backend) + area weather, fetched for the current
    // viewport and refreshed whenever the map stops moving — the "real-time" feel.
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
            // Anchor the scan on the user's real GPS position when we have it;
            // fall back to the map center only until geolocation resolves.
            const mapCenter = m.getCenter?.();
            const center = userLocation ?? (mapCenter ? { lat: mapCenter.lat, lng: mapCenter.lng } : null);
            if (!center) return;
            const radius = NEARBY_RADIUS_METERS;
            const [places, weather] = await Promise.all([
                nearbyApi.search(center.lat, center.lng, radius, 40),
                weatherApi.getWeather(center.lat, center.lng),
            ]);
            if (cancelled) return;
            if (places.data) setNearbyItems(places.data.items);
            if (weather.data) {
                setAreaWeather(normalizeWeather(weather.data.condition));
                setAreaInfo({
                    temp: weather.data.temp,
                    uvi: weather.data.uvi,
                    rain_mm: weather.data.rain_mm,
                });
            }
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
    }, [mapLoaded, userLocation]);

    // Sidebar list = live POIs filtered by category chip + search query.
    const filteredPlaces = useMemo(() => {
        const q = searchQuery.toLowerCase();
        return nearbyItems.filter((p) => {
            const matchCat = activeCategory === 'all' || p.category === activeCategory;
            const matchSearch = q === '' ||
                p.name.toLowerCase().includes(q) ||
                p.category_label.toLowerCase().includes(q) ||
                (p.address ?? '').toLowerCase().includes(q);
            return matchCat && matchSearch;
        });
    }, [nearbyItems, activeCategory, searchQuery]);

    // Render the same filtered set on the map as dynamic bubbles.
    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;
        setSourceData(
            mapRef.current,
            'bv-nearby',
            nearbyFeatureCollection(filteredPlaces, selectedPlace?.id ?? null, areaWeather, userLocation),
        );
    }, [filteredPlaces, selectedPlace, areaWeather, mapLoaded, userLocation]);

    const handleSelectPlace = useCallback((place: NearbyPlace) => {
        setSelectedPlace(place);
        mapRef.current?.flyTo({ center: [place.longitude, place.latitude], zoom: 16, speed: 1.4 });
    }, []);

    // Locate the user and drop a "you are here" marker
    const handleLocate = useCallback(() => {
        if (!navigator.geolocation || !mapRef.current || !goongRef.current) return;
        setLocating(true);
        setLocationError('');
        navigator.geolocation.getCurrentPosition(
            (pos) => {
                const { longitude, latitude } = pos.coords;
                setUserLocation({ lat: latitude, lng: longitude });
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

    // Auto-locate once the map is ready so the scan centers on the real user
    // position instead of the default map view.
    useEffect(() => {
        if (!mapLoaded) return;
        handleLocate();
    }, [mapLoaded, handleLocate]);

    // Clicking a bubble/pin selects that POI.
    useEffect(() => {
        if (!mapLoaded || !mapRef.current) return;

        const map = mapRef.current as ExploreInteractiveMap;
        const layerIds = ['bv-nearby-bubble', 'bv-nearby-pin'];

        const handleClick = (event: ExploreLayerClickEvent) => {
            const id = event.features?.[0]?.properties?.id;
            const place = nearbyItems.find((item) => item.id === id);
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
    }, [mapLoaded, handleSelectPlace, nearbyItems]);

    const selectedWeather = selectedPlace ? areaWeather : null;
    const selectedHasLiveWeather = Boolean(selectedPlace);
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
                    {filteredPlaces.map((place) => (
                        <button
                            key={place.id}
                            className={`${styles.placeItem} ${selectedPlace?.id === place.id ? styles.placeItemSelected : ''}`}
                            onClick={() => handleSelectPlace(place)}
                        >
                            <div className={styles.placeInfo}>
                                <div className={styles.placeName}>{place.name}</div>
                                <div className={styles.placeRegion}>{place.category_label} · {CATEGORY_LABEL[place.category]}</div>
                                {place.address && <div className={styles.placeHint}>{place.address}</div>}
                            </div>
                            <div className={styles.placeRightMeta}>
                                {effectiveDistance(place, userLocation) != null && (
                                    <span className={styles.placeRating}>{formatDistance(effectiveDistance(place, userLocation)!)}</span>
                                )}
                                <span className={styles.placeWeatherText}>{WEATHER_LABELS[areaWeather]} · live</span>
                            </div>
                        </button>
                    ))}
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

                {/* Area weather: current condition, temp, estimated UV, rain */}
                {mapLoaded && areaInfo && (
                    <div
                        style={{
                            position: 'absolute', top: 16, right: 16, zIndex: 5,
                            display: 'flex', gap: 10, alignItems: 'center',
                            padding: '8px 14px', borderRadius: 999,
                            background: 'rgba(26, 18, 11, 0.82)', backdropFilter: 'blur(6px)',
                            border: '1px solid rgba(198, 154, 63, 0.35)',
                            color: '#ece0c6', fontSize: '0.8rem', fontWeight: 600,
                            pointerEvents: 'none', whiteSpace: 'nowrap',
                        }}
                    >
                        <span>{WEATHER_LABELS[areaWeather]}</span>
                        {areaInfo.temp != null && <span>· {Math.round(areaInfo.temp)}°</span>}
                        {areaInfo.uvi != null && (
                            <span title="UV ước tính">· UV {areaInfo.uvi} ({uvLabel(areaInfo.uvi)})</span>
                        )}
                        <span>· Mưa {areaInfo.rain_mm != null ? `${areaInfo.rain_mm} mm` : '0'}</span>
                    </div>
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
                                    <div className={styles.detailCardTitle}>{selectedPlace.name}</div>
                                    <div className={styles.detailCardSubtitle}>{selectedPlace.category_label}</div>
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
                                {effectiveDistance(selectedPlace, userLocation) != null && (
                                    <span className={styles.detailCardRating}>
                                        {formatDistance(effectiveDistance(selectedPlace, userLocation)!)}
                                    </span>
                                )}
                            </div>

                            {selectedPlace.address && (
                                <p className={styles.detailCardDesc}>{selectedPlace.address}</p>
                            )}

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
