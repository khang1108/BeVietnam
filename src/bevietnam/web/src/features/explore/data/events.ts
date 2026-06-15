import type { Locale } from '@/i18n';

export type EventStatus = 'ongoing' | 'completed' | 'upcoming';

export type EventDate = {
    start: string;
    end?: string;
    time: string;
    label: string;
};

export type EventPrice = {
    amount: number;
    currency: 'VND';
    label: Record<Locale, string>;
};

export type CulturalEvent = {
    id: string;
    name: Record<Locale, string>;
    date: EventDate;
    place: {
        summary: Record<Locale, string>;
        location: Record<Locale, string>;
        venue: Record<Locale, string>;
    };
    price: EventPrice;
    description: Record<Locale, string>;
    about: Record<Locale, string>;
    image: string;
};

export const eventStatusLabels: Record<EventStatus, Record<Locale, string>> = {
    ongoing: {
        vi: 'Đang diễn ra',
        en: 'Ongoing',
    },
    completed: {
        vi: 'Đã diễn ra',
        en: 'Completed',
    },
    upcoming: {
        vi: 'Sắp diễn ra',
        en: 'Upcoming',
    },
};

export const events: CulturalEvent[] = [
    {
        id: '1',
        name: {
            vi: 'Lễ hội Áo dài TP.HCM 2026',
            en: 'HCMC Ao Dai Festival 2026',
        },
        date: {
            start: '2026-05-15',
            end: '2026-05-20',
            time: '18:00 - 22:00',
            label: '15-20 May 2026',
        },
        place: {
            summary: {
                vi: 'TP.HCM',
                en: 'HCMC',
            },
            location: {
                vi: 'TP. Hồ Chí Minh, Việt Nam',
                en: 'Ho Chi Minh City, Vietnam',
            },
            venue: {
                vi: 'Phố đi bộ Nguyễn Huệ',
                en: 'Nguyen Hue Walking Street',
            },
        },
        price: {
            amount: 0,
            currency: 'VND',
            label: {
                vi: 'Miễn phí',
                en: 'Free admission',
            },
        },
        description: {
            vi: 'Lễ hội tôn vinh trang phục truyền thống Việt Nam với trình diễn thời trang và văn hóa.',
            en: 'Festival celebrating traditional Vietnamese dress with fashion shows and cultural events.',
        },
        about: {
            vi: 'Lễ hội Áo dài TP.HCM 2026 là sự kiện văn hóa thường niên tôn vinh vẻ đẹp của trang phục truyền thống Việt Nam. Chương trình bao gồm trình diễn thời trang, triển lãm áo dài qua các thời kỳ, workshop thiết kế và nhiều hoạt động nghệ thuật phong phú.',
            en: 'HCMC Ao Dai Festival 2026 is an annual cultural event celebrating the beauty of traditional Vietnamese dress. The program includes fashion shows, exhibitions of ao dai through the ages, design workshops, and various art activities.',
        },
        image: '/images/event-aodai.png',
    },
    {
        id: '2',
        name: {
            vi: 'Festival Huế 2026',
            en: 'Hue Festival 2026',
        },
        date: {
            start: '2026-06-01',
            end: '2026-06-07',
            time: '08:00 - 22:00',
            label: '01-07 Jun 2026',
        },
        place: {
            summary: {
                vi: 'Huế',
                en: 'Hue',
            },
            location: {
                vi: 'Huế, Việt Nam',
                en: 'Hue, Vietnam',
            },
            venue: {
                vi: 'Đại Nội Huế và các khu vực lân cận',
                en: 'Hue Imperial Citadel and surrounding areas',
            },
        },
        price: {
            amount: 250000,
            currency: 'VND',
            label: {
                vi: 'Có phí',
                en: 'Paid ticket',
            },
        },
        description: {
            vi: 'Lễ hội văn hóa quốc tế lớn nhất miền Trung, tổ chức 2 năm một lần tại cố đô Huế.',
            en: 'The largest international cultural festival in Central Vietnam, held biennially in the ancient capital.',
        },
        about: {
            vi: 'Festival Huế 2026 quy tụ các đoàn nghệ thuật đặc sắc từ Việt Nam và quốc tế. Điểm nhấn là nghệ thuật cung đình, lễ hội đường phố, trình diễn áo dài và các triển lãm di sản tại cố đô.',
            en: 'Hue Festival 2026 brings together outstanding Vietnamese and international art troupes. Highlights include imperial performances, street festivals, ao dai shows, and heritage exhibitions in the ancient capital.',
        },
        image: '/images/event-hue.png',
    },
    {
        id: '3',
        name: {
            vi: 'Đêm nhạc Jazz Sài Gòn',
            en: 'Saigon Jazz Night',
        },
        date: {
            start: '2026-06-10',
            time: '19:30 - 22:30',
            label: '10 Jun 2026',
        },
        place: {
            summary: {
                vi: 'TP.HCM',
                en: 'HCMC',
            },
            location: {
                vi: 'TP. Hồ Chí Minh, Việt Nam',
                en: 'Ho Chi Minh City, Vietnam',
            },
            venue: {
                vi: 'Nhà hát Thành phố',
                en: 'Ho Chi Minh City Opera House',
            },
        },
        price: {
            amount: 350000,
            currency: 'VND',
            label: {
                vi: 'Có phí',
                en: 'Paid ticket',
            },
        },
        description: {
            vi: 'Đêm nhạc Jazz với sự tham gia của nghệ sĩ quốc tế và Việt Nam tại Nhà hát Thành phố.',
            en: 'Jazz night featuring international and Vietnamese artists at the City Opera House.',
        },
        about: {
            vi: 'Đêm nhạc Jazz Sài Gòn mang đến không gian âm nhạc tinh tế với các nghệ sĩ Việt Nam và quốc tế, kết hợp saxophone, piano và vocal trong một hành trình giàu cảm xúc.',
            en: 'Saigon Jazz Night offers an elegant music experience with Vietnamese and international artists, blending saxophone, piano, and vocals into an expressive journey.',
        },
        image: '/images/event-jazz.png',
    },
    {
        id: '4',
        name: {
            vi: 'Chợ phiên ẩm thực đường phố',
            en: 'Street Food Market Fair',
        },
        date: {
            start: '2026-06-20',
            end: '2026-06-22',
            time: '16:00 - 23:00',
            label: '20-22 Jun 2026',
        },
        place: {
            summary: {
                vi: 'TP.HCM',
                en: 'HCMC',
            },
            location: {
                vi: 'TP. Hồ Chí Minh, Việt Nam',
                en: 'Ho Chi Minh City, Vietnam',
            },
            venue: {
                vi: 'Công viên 23/9',
                en: 'September 23rd Park',
            },
        },
        price: {
            amount: 0,
            currency: 'VND',
            label: {
                vi: 'Miễn phí',
                en: 'Free admission',
            },
        },
        description: {
            vi: 'Tập hợp hơn 50 gian hàng ẩm thực đường phố từ khắp Việt Nam tại công viên 23/9.',
            en: 'Over 50 street food stalls from all over Vietnam gathered at September 23rd Park.',
        },
        about: {
            vi: 'Chợ phiên quy tụ hơn 50 gian hàng ẩm thực đường phố từ ba miền, từ bánh mì, xiên nướng đến chè và các món ăn đặc trưng trong không khí náo nhiệt.',
            en: 'The fair gathers over 50 street food stalls from across Vietnam, from banh mi and grilled skewers to sweet soups and regional specialties in a lively atmosphere.',
        },
        image: '/images/event-streetfood.png',
    },
    {
        id: '5',
        name: {
            vi: 'Triển lãm nghệ thuật đương đại',
            en: 'Contemporary Art Exhibition',
        },
        date: {
            start: '2026-07-05',
            end: '2026-07-21',
            time: '09:00 - 18:00',
            label: '05-21 Jul 2026',
        },
        place: {
            summary: {
                vi: 'Hà Nội',
                en: 'Hanoi',
            },
            location: {
                vi: 'Hà Nội, Việt Nam',
                en: 'Hanoi, Vietnam',
            },
            venue: {
                vi: 'Bảo tàng Mỹ thuật Việt Nam',
                en: 'Vietnam Fine Arts Museum',
            },
        },
        price: {
            amount: 0,
            currency: 'VND',
            label: {
                vi: 'Miễn phí',
                en: 'Free admission',
            },
        },
        description: {
            vi: 'Triển lãm tranh và điêu khắc của các nghệ sĩ trẻ Việt Nam tại Bảo tàng Mỹ thuật.',
            en: 'Painting and sculpture exhibition by young Vietnamese artists at the Fine Arts Museum.',
        },
        about: {
            vi: 'Triển lãm giới thiệu tranh, sắp đặt và điêu khắc của các nghệ sĩ trẻ Việt Nam, kết nối chất liệu truyền thống với cách nhìn đương đại.',
            en: 'The exhibition presents paintings, installations, and sculptures by young Vietnamese artists, connecting traditional materials with contemporary perspectives.',
        },
        image: '/images/event-art.png',
    },
];

const monthLabels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

function toDateKey(date: Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

export function getEventStatus(date: EventDate, today = new Date()): EventStatus {
    const todayKey = toDateKey(today);
    const end = date.end ?? date.start;

    if (todayKey < date.start) {
        return 'upcoming';
    }

    if (todayKey > end) {
        return 'completed';
    }

    return 'ongoing';
}

export function getEventStatusLabel(status: EventStatus, locale: Locale) {
    return eventStatusLabels[status][locale];
}

export function getEventDateMonth(date: EventDate) {
    const monthIndex = Number(date.start.slice(5, 7)) - 1;

    return monthLabels[monthIndex] ?? '';
}

export function getEventDateDay(date: EventDate) {
    return date.start.slice(8, 10);
}

export function isFreeEvent(price: EventPrice) {
    return price.amount === 0;
}
