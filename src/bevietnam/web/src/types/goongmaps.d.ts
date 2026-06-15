declare module '@goongmaps/goong-js' {
    interface MapOptions {
        container: HTMLElement | string;
        style?: string;
        center?: [number, number];
        zoom?: number;
        [key: string]: unknown;
    }

    class Map {
        constructor(options: MapOptions);
        on(event: string, handler: (...args: unknown[]) => void): this;
        off(event: string, handler: (...args: unknown[]) => void): this;
        remove(): void;
        flyTo(options: { center: [number, number]; zoom?: number; speed?: number }): this;
        isStyleLoaded(): boolean;
        addControl(control: unknown, position?: string): this;
    }

    interface MarkerOptions {
        element?: HTMLElement;
        anchor?: string;
    }

    class Marker {
        constructor(options?: MarkerOptions);
        setLngLat(lngLat: [number, number]): this;
        addTo(map: Map): this;
        remove(): void;
        getElement(): HTMLElement;
    }

    class NavigationControl {
        constructor(options?: { showCompass?: boolean; showZoom?: boolean });
    }

    let accessToken: string;

    export { Map, Marker, NavigationControl, accessToken };
}
