'use client';

import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from 'react';
import type { Map, Marker } from '@goongmaps/goong-js';
import styles from '../styles/explore.module.css';
import { weatherApi, type WeatherCondition as ApiWeatherCondition } from '@/lib/api';

// ─── Types ────────────────────────────────────────────────────────────────────

type WeatherCondition = 'sunny' | 'cloudy' | 'rainy';
type Category = 'history' | 'culture' | 'food' | 'nature' | 'festival' | 'amusement';

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

// ─── Constants ────────────────────────────────────────────────────────────────

// Bubble diameter (px) by weather — clear tiers so it's obvious at a glance.
const BUBBLE_SIZE: Record<WeatherCondition, number> = {
    sunny: 78,   // Nắng đẹp → đông khách
    cloudy: 52,  // Có mây → bình thường
    rainy: 34,   // Mưa → vắng khách
};

const WEATHER_COLORS: Record<WeatherCondition, { fill: string; ring: string }> = {
    sunny:  { fill: 'rgba(251,191,36,0.55)',  ring: '#fbbf24' },
    cloudy: { fill: 'rgba(96,165,250,0.5)',   ring: '#60a5fa' },
    rainy:  { fill: 'rgba(167,139,250,0.5)',  ring: '#a78bfa' },
};

// Pin colour per category (the teardrop fill — Google-style)
const CATEGORY_COLORS: Record<Category, string> = {
    history:  '#e6b422',
    culture:  '#c9302c',
    food:     '#f97316',
    nature:   '#2d9d6f',
    festival: '#d946ef',
    amusement:'#3b82f6',
};

const CATEGORY_ICONS: Record<Category, string> = {
    history:  '🏛️',
    culture:  '🏮',
    food:     '🍜',
    nature:   '🌿',
    festival: '🎆',
    amusement:'🎡',
};

const WEATHER_ICONS: Record<WeatherCondition, string> = {
    sunny:  '☀️',
    cloudy: '⛅',
    rainy:  '🌧️',
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
};

const FILTER_CHIPS = [
    { key: 'all',       label: 'Tất cả', icon: '🗺️' },
    { key: 'history',   label: 'Di tích', icon: '🏛️' },
    { key: 'culture',   label: 'Văn hóa', icon: '🏮' },
    { key: 'food',      label: 'Ẩm thực', icon: '🍜' },
    { key: 'nature',    label: 'Thiên nhiên', icon: '🌿' },
    { key: 'amusement', label: 'Vui chơi', icon: '🎡' },
    { key: 'festival',  label: 'Lễ hội', icon: '🎆' },
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

const GOONG_API_KEY = process.env.NEXT_PUBLIC_GOONG_API_KEY ?? '';

// Build a Google-style teardrop pin SVG (tip exactly at bottom-center).
function pinSvg(fill: string): string {
    return `<svg class="bv-marker-pin" width="36" height="46" viewBox="0 0 36 46" xmlns="http://www.w3.org/2000/svg">
        <path d="M18 45 C8 31 4 24 4 15.5 A14 14 0 1 1 32 15.5 C32 24 28 31 18 45 Z"
              fill="${fill}" stroke="#ffffff" stroke-width="2.5"/>
    </svg>`;
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
    const markersRef = useRef<Marker[]>([]);
    const currentMarkerRef = useRef<Marker | null>(null);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const goongRef = useRef<any>(null);

    const [mapLoaded, setMapLoaded] = useState(false);
    const [selectedPlace, setSelectedPlace] = useState<Place | null>(null);
    const [activeCategory, setActiveCategory] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [locating, setLocating] = useState(false);
    const [weatherByPlace, setWeatherByPlace] = useState<Record<string, ApiWeatherCondition>>({});

    // Lock body scroll for full-screen map
    useEffect(() => {
        document.body.style.overflow = 'hidden';
        return () => { document.body.style.overflow = ''; };
    }, []);

    // Initialize Goong map — loaded dynamically to avoid SSR issues
    useEffect(() => {
        const container = mapContainerRef.current;
        if (!container || !GOONG_API_KEY || mapRef.current) return;

        let map: Map;

        const initMap = async () => {
            // goong-js is CJS/UMD — dynamic import wraps it under .default
            const mod = await import('@goongmaps/goong-js');
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const G = ((mod as any).default ?? mod) as any;

            // Goong JS requires accessToken to be set before creating Map
            G.accessToken = GOONG_API_KEY;
            goongRef.current = G;

            map = new G.Map({
                container,
                style: `https://tiles.goong.io/assets/goong_map_dark.json?api_key=${GOONG_API_KEY}`,
                center: [107.95, 16.15],
                zoom: 8,
            }) as Map;

            if (G.NavigationControl) {
                map.addControl(new G.NavigationControl({ showCompass: false }), 'bottom-right');
            }
            mapRef.current = map;

            map.on('load', () => {
                hideBasePoiIcons(map);
                fitToPlaces(map);
                setMapLoaded(true);
            });
        };

        initMap();

        return () => {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (map as any)?.remove();
            mapRef.current = null;
            setMapLoaded(false);
        };
    }, []);

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
        setSidebarOpen(false);
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
                alert('Không thể lấy vị trí của bạn. Hãy kiểm tra quyền truy cập vị trí trong trình duyệt.');
            },
            { enableHighAccuracy: true, timeout: 8000 },
        );
    }, []);

    // Recreate markers whenever filtered places or selection changes
    useEffect(() => {
        if (!mapLoaded || !mapRef.current || !goongRef.current) return;

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const G = goongRef.current as any;

        markersRef.current.forEach((m) => (m as Marker).remove());
        markersRef.current = [];

        filteredPlaces.forEach((place) => {
            const isSelected = place.id === selectedPlace?.id;
            const placeWeather = getPlaceWeather(place, weatherByPlace);
            const wc = WEATHER_COLORS[placeWeather];
            const bubble = Math.round(BUBBLE_SIZE[placeWeather] * (isSelected ? 1.15 : 1));
            const pinFill = CATEGORY_COLORS[place.category];
            const icon = CATEGORY_ICONS[place.category];

            const wrap = document.createElement('div');
            wrap.className = `bv-marker${isSelected ? ' bv-marker-selected' : ''}`;

            // Weather bubble (crowd indicator) — soft glow sized by weather
            const bub = document.createElement('div');
            bub.className = 'bv-marker-bubble';
            bub.style.width = `${bubble}px`;
            bub.style.height = `${bubble}px`;
            bub.style.background = `radial-gradient(circle at 50% 50%, ${wc.fill}, transparent 72%)`;
            bub.style.border = `1.5px solid ${wc.ring}`;

            // Pulsing ring
            const pulse = document.createElement('div');
            pulse.className = 'bv-marker-pulse';
            pulse.style.width = `${bubble}px`;
            pulse.style.height = `${bubble}px`;
            pulse.style.borderColor = wc.ring;

            // Google-style pin + category icon
            const pinWrap = document.createElement('div');
            pinWrap.innerHTML = pinSvg(pinFill);
            const pin = pinWrap.firstElementChild as HTMLElement;

            const iconEl = document.createElement('div');
            iconEl.className = 'bv-marker-icon';
            iconEl.textContent = icon;

            wrap.appendChild(pulse);
            wrap.appendChild(bub);
            if (pin) wrap.appendChild(pin);
            wrap.appendChild(iconEl);

            wrap.addEventListener('click', (e) => {
                e.stopPropagation();
                handleSelectPlace(place);
            });

            const marker: Marker = new G.Marker({ element: wrap, anchor: 'bottom' })
                .setLngLat([place.lng, place.lat])
                .addTo(mapRef.current!);

            markersRef.current.push(marker);
        });
    }, [mapLoaded, filteredPlaces, selectedPlace, handleSelectPlace, weatherByPlace]);

    const selectedWeather = selectedPlace ? getPlaceWeather(selectedPlace, weatherByPlace) : null;
    const selectedHasLiveWeather = selectedPlace ? hasLiveWeather(selectedPlace, weatherByPlace) : false;
    const weatherBadgeClass = selectedWeather
        ? styles[`badge${selectedWeather.charAt(0).toUpperCase() + selectedWeather.slice(1)}` as keyof typeof styles]
        : '';

    return (
        <div className={styles.mapPage}>

            {/* Mobile sidebar toggle */}
            <button
                className={styles.sidebarToggle}
                onClick={() => setSidebarOpen(true)}
                aria-label="Mở thanh bên"
            >
                ☰
            </button>

            {/* Mobile overlay */}
            {sidebarOpen && (
                <div
                    className={styles.sidebarOverlay}
                    onClick={() => setSidebarOpen(false)}
                />
            )}

            {/* ── Sidebar ── */}
            <aside className={`${styles.sidebar} ${sidebarOpen ? styles.sidebarOpen : ''}`}>
                <div className={styles.sidebarHeader}>
                    <div className={styles.sidebarBrand}>
                        <div className={styles.sidebarBrandIcon}>🗺️</div>
                        <span className={styles.sidebarTitle}>
                            Be<span className={styles.sidebarTitleAccent}>Vietnam</span> Map
                        </span>
                    </div>
                    <div className={styles.sidebarSubtitle}>
                        Khám phá di sản & văn hoá Việt Nam
                    </div>
                </div>

                {/* Search */}
                <div className={styles.searchContainer}>
                    <div className={styles.searchInputWrapper}>
                        <span className={styles.searchIcon}>🔍</span>
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
                                ✕
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
                                <span>{chip.icon}</span>
                                {chip.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Results count */}
                <div className={styles.resultsMeta}>
                    {filteredPlaces.length} địa điểm
                </div>

                {/* Places list */}
                <div className={styles.placesList}>
                    {filteredPlaces.map((place) => {
                        const placeWeather = getPlaceWeather(place, weatherByPlace);
                        const placeHasLiveWeather = hasLiveWeather(place, weatherByPlace);
                        return (
                            <div
                                key={place.id}
                                className={`${styles.placeItem} ${selectedPlace?.id === place.id ? styles.placeItemSelected : ''}`}
                                onClick={() => handleSelectPlace(place)}
                            >
                                <div className={styles.placeIcon}>
                                    {CATEGORY_ICONS[place.category]}
                                </div>
                                <div className={styles.placeInfo}>
                                    <div className={styles.placeName}>{place.title}</div>
                                    <div className={styles.placeRegion}>{place.region} · {CATEGORY_LABEL[place.category]}</div>
                                </div>
                                <div className={styles.placeRightMeta}>
                                    <span className={styles.placeRating}>⭐ {place.rating}</span>
                                    <span className={styles.placeWeatherIcon}>{WEATHER_ICONS[placeWeather]}</span>
                                    {placeHasLiveWeather && (
                                        <span className={styles.placeWeatherLive}>live</span>
                                    )}
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Weather legend */}
                <div className={styles.weatherLegend}>
                    <div className={styles.weatherLegendTitle}>Kích thước bong bóng theo thời tiết</div>
                    <div className={styles.weatherLegendItems}>
                        <div className={styles.weatherLegendItem}>
                            <div className={`${styles.weatherBubble} ${styles.weatherBubbleSunny}`} />
                            <div className={styles.weatherLegendLabel}>
                                <span className={styles.weatherLegendName}>☀️ Nắng</span>
                                <span className={styles.weatherLegendHint}>Đông khách</span>
                            </div>
                        </div>
                        <div className={styles.weatherLegendItem}>
                            <div className={`${styles.weatherBubble} ${styles.weatherBubbleCloudy}`} />
                            <div className={styles.weatherLegendLabel}>
                                <span className={styles.weatherLegendName}>⛅ Mây</span>
                                <span className={styles.weatherLegendHint}>Bình thường</span>
                            </div>
                        </div>
                        <div className={styles.weatherLegendItem}>
                            <div className={`${styles.weatherBubble} ${styles.weatherBubbleRainy}`} />
                            <div className={styles.weatherLegendLabel}>
                                <span className={styles.weatherLegendName}>🌧️ Mưa</span>
                                <span className={styles.weatherLegendHint}>Vắng khách</span>
                            </div>
                        </div>
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
                            {locating ? '◎' : '⌖'}
                        </span>
                    </button>
                )}

                {/* Overlay: API key missing */}
                {!GOONG_API_KEY && (
                    <div className={styles.mapStatus}>
                        <div className={styles.mapStatusIcon}>🗺️</div>
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

                {/* Overlay: Map loading */}
                {GOONG_API_KEY && !mapLoaded && (
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
                                <div className={styles.detailCardIcon}>
                                    {CATEGORY_ICONS[selectedPlace.category]}
                                </div>
                                <div className={styles.detailCardMeta}>
                                    <div className={styles.detailCardTitle}>{selectedPlace.title}</div>
                                    <div className={styles.detailCardSubtitle}>{selectedPlace.subtitle}</div>
                                </div>
                                <button
                                    className={styles.detailCardClose}
                                    onClick={() => setSelectedPlace(null)}
                                    aria-label="Đóng"
                                >
                                    ✕
                                </button>
                            </div>

                            <div className={styles.detailCardBadges}>
                                <span className={`${styles.badge} ${styles.badgeCategory}`}>
                                    {CATEGORY_LABEL[selectedPlace.category]}
                                </span>
                                {selectedWeather && (
                                    <span className={`${styles.badge} ${weatherBadgeClass}`}>
                                        {WEATHER_ICONS[selectedWeather]} {WEATHER_LABELS[selectedWeather]}
                                    </span>
                                )}
                                {selectedHasLiveWeather && (
                                    <span className={`${styles.badge} ${styles.badgeLive}`}>
                                        live
                                    </span>
                                )}
                                <span className={styles.detailCardRating}>
                                    ⭐ {selectedPlace.rating}
                                </span>
                            </div>

                            <p className={styles.detailCardDesc}>{selectedPlace.description}</p>

                            <div className={styles.detailCardActions}>
                                <button className={styles.detailCardBtnPrimary}>
                                    🧭 Bắt đầu Quest
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
