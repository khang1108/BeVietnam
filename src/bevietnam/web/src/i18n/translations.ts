export type Locale = 'vi' | 'en';

export interface Translations {
  [key: string]: string | Translations;
}

const vi: Translations = {
  common: {
    appName: 'BeVietnam',
    tagline: 'Khám phá Việt Nam thông minh',
    loading: 'Đang tải...',
    error: 'Có lỗi xảy ra',
    retry: 'Thử lại',
    search: 'Tìm kiếm',
    viewAll: 'Xem tất cả',
    readMore: 'Xem thêm',
    back: 'Quay lại',
    submit: 'Gửi',
    cancel: 'Hủy',
    save: 'Lưu',
    close: 'Đóng',
  },
  nav: {
    feed: 'Bảng tin',
    explore: 'Khám phá',
    storyline: 'Hành trình',
    events: 'Sự kiện',
    contribute: 'Đóng góp',
    login: 'Đăng nhập',
    register: 'Đăng ký',
    profile: 'Hồ sơ',
    logout: 'Đăng xuất',
    theme: 'Giao diện',
    language: 'Ngôn ngữ',
  },
  feed: {
    title: 'Bảng tin của bạn',
    subtitle: 'Khám phá những điều thú vị xung quanh bạn',
    empty: 'Chưa có bài viết nào',
    trending: 'Xu hướng',
    nearby: 'Gần bạn',
    recommended: 'Gợi ý cho bạn',
  },
  explore: {
    title: 'Khám phá địa điểm',
    subtitle: 'Tìm những điểm đến tuyệt vời trên khắp Việt Nam',
    searchPlaceholder: 'Tìm kiếm địa điểm, món ăn...',
    filters: 'Bộ lọc',
    mapView: 'Xem bản đồ',
    listView: 'Xem danh sách',
    categories: {
      all: 'Tất cả',
      food: 'Ẩm thực',
      culture: 'Văn hóa',
      nature: 'Thiên nhiên',
      history: 'Lịch sử',
      nightlife: 'Về đêm',
    },
  },
  place: {
    details: 'Chi tiết',
    reviews: 'Đánh giá',
    culturalContext: 'Bối cảnh văn hóa',
    openingHours: 'Giờ mở cửa',
    address: 'Địa chỉ',
    phone: 'Điện thoại',
    website: 'Website',
    directions: 'Chỉ đường',
    share: 'Chia sẻ',
    photos: 'Hình ảnh',
  },
  events: {
    title: 'Sự kiện',
    subtitle: 'Các sự kiện đang diễn ra và sắp tới',
    upcoming: 'Sắp diễn ra',
    ongoing: 'Đang diễn ra',
    past: 'Đã kết thúc',
    date: 'Ngày',
    location: 'Địa điểm',
    free: 'Miễn phí',
  },
  contribute: {
    title: 'Đóng góp cộng đồng',
    subtitle: 'Chia sẻ những địa điểm, món ăn yêu thích của bạn',
    form: {
      name: 'Tên địa điểm',
      description: 'Mô tả',
      category: 'Danh mục',
      address: 'Địa chỉ',
      photos: 'Hình ảnh',
      submit: 'Gửi đóng góp',
    },
    success: 'Cảm ơn bạn! Đóng góp sẽ được xét duyệt.',
    guidelines: 'Hướng dẫn đóng góp',
  },
  auth: {
    login: {
      title: 'Đăng nhập',
      email: 'Email',
      password: 'Mật khẩu',
      forgotPassword: 'Quên mật khẩu?',
      noAccount: 'Chưa có tài khoản?',
      signUpHere: 'Đăng ký ngay',
    },
    register: {
      title: 'Đăng ký',
      name: 'Họ và tên',
      email: 'Email',
      password: 'Mật khẩu',
      confirmPassword: 'Xác nhận mật khẩu',
      hasAccount: 'Đã có tài khoản?',
      loginHere: 'Đăng nhập ngay',
    },
  },
  storyline: {
    kieuKyTag: 'Ký sự hành trình',
    mainTitlePrefix: 'Di Sản',
    mainTitleSuffix: 'Việt Nam',
    subTitle: 'Khám phá những miền di sản, văn hóa và vẻ đẹp tiềm ẩn của dải đất hình chữ S. Mỗi chặng dừng chân là một câu chuyện vô giá.',
    noteThangLong: 'Bắt đầu hành trình từ Thăng Long ngàn năm văn hiến...',
    noteSea: 'Cẩn thận những cơn bão biển!',
    exploreBtn: 'Khám phá',
    lockedText: 'Chưa khám phá',
    quests: {
      quest1: {
        title: 'Hồ Hoàn Kiếm',
        desc: 'Trái tim của Thủ đô, nơi gắn liền với truyền thuyết rùa vàng trả gươm báu. Dạo bước quanh hồ và cảm nhận nhịp sống chậm rãi.',
      },
      quest2: {
        title: 'Vịnh Hạ Long',
        desc: 'Kỳ quan thiên nhiên thế giới với hàng ngàn đảo đá vôi kỳ vĩ vươn lên từ mặt nước xanh ngọc bích.',
      },
      quest3: {
        title: 'Phố Cổ Hội An',
        desc: 'Thương cảng sầm uất một thời, nay lung linh trong ánh đèn lồng lụa và những nếp nhà ngói âm dương rêu phong.',
      },
      quest4: {
        title: 'Kinh Thành Huế',
        desc: 'Dấu ấn triều đại xưa, nơi lăng tẩm hoàng gia và nhã nhạc cung đình đưa bạn trở về những trang sử hào hùng.',
      },
      quest5: {
        title: 'Mù Cang Chải',
        desc: 'Những thửa ruộng bậc thang kỳ vĩ dệt nên tấm thảm lụa vàng ươm vắt ngang lưng trời Tây Bắc.',
      },
    },
    title: 'Hành trình BeVietnam',
    subtitle: 'Hành trình văn hoá qua các miền di sản Việt Nam',
    difficulty: 'Độ khó',
    requirement: 'Yêu cầu hoàn thành',
    completeTask: 'Hoàn Thành Nhiệm Vụ',
    completed: '✓ Đã Hoàn Thành',
    locked: 'Chưa mở khóa',
    reset: 'Đặt lại Hành trình',
    chapter: 'Chương',
    done: 'Hoàn thành',
    doing: 'Đang làm',
    nextTask: 'Nhiệm vụ tiếp theo',
    easy: 'Dễ',
    medium: 'Trung bình',
    hard: 'Khó'
  },
  footer: {
    about: 'Về BeVietnam',
    aboutDesc: 'Nền tảng du lịch thông minh giúp bạn khám phá Việt Nam với chiều sâu văn hóa.',
    quickLinks: 'Liên kết nhanh',
    contact: 'Liên hệ',
    copyright: '© 2026 BeVietnam. Bản quyền thuộc về nhóm phát triển.',
    madeWith: 'Được xây dựng với ❤️ tại Việt Nam',
  },
};

const en: Translations = {
  common: {
    appName: 'BeVietnam',
    tagline: 'Smart Vietnam Discovery',
    loading: 'Loading...',
    error: 'An error occurred',
    retry: 'Retry',
    search: 'Search',
    viewAll: 'View all',
    readMore: 'Read more',
    back: 'Go back',
    submit: 'Submit',
    cancel: 'Cancel',
    save: 'Save',
    close: 'Close',
  },
  nav: {
    feed: 'Feed',
    explore: 'Explore',
    storyline: 'Storyline',
    events: 'Events',
    contribute: 'Contribute',
    login: 'Login',
    register: 'Register',
    profile: 'Profile',
    logout: 'Logout',
    theme: 'Theme',
    language: 'Language',
  },
  feed: {
    title: 'Your Feed',
    subtitle: 'Discover interesting things around you',
    empty: 'No posts yet',
    trending: 'Trending',
    nearby: 'Nearby',
    recommended: 'Recommended for you',
  },
  explore: {
    title: 'Explore Places',
    subtitle: 'Find amazing destinations across Vietnam',
    searchPlaceholder: 'Search places, food...',
    filters: 'Filters',
    mapView: 'Map view',
    listView: 'List view',
    categories: {
      all: 'All',
      food: 'Food',
      culture: 'Culture',
      nature: 'Nature',
      history: 'History',
      nightlife: 'Nightlife',
    },
  },
  place: {
    details: 'Details',
    reviews: 'Reviews',
    culturalContext: 'Cultural Context',
    openingHours: 'Opening Hours',
    address: 'Address',
    phone: 'Phone',
    website: 'Website',
    directions: 'Directions',
    share: 'Share',
    photos: 'Photos',
  },
  events: {
    title: 'Events',
    subtitle: 'Ongoing and upcoming events',
    upcoming: 'Upcoming',
    ongoing: 'Ongoing',
    past: 'Past',
    date: 'Date',
    location: 'Location',
    free: 'Free',
  },
  contribute: {
    title: 'Community Contributions',
    subtitle: 'Share your favorite places and food discoveries',
    form: {
      name: 'Place name',
      description: 'Description',
      category: 'Category',
      address: 'Address',
      photos: 'Photos',
      submit: 'Submit contribution',
    },
    success: 'Thank you! Your contribution will be reviewed.',
    guidelines: 'Contribution guidelines',
  },
  auth: {
    login: {
      title: 'Login',
      email: 'Email',
      password: 'Password',
      forgotPassword: 'Forgot password?',
      noAccount: "Don't have an account?",
      signUpHere: 'Sign up here',
    },
    register: {
      title: 'Register',
      name: 'Full name',
      email: 'Email',
      password: 'Password',
      confirmPassword: 'Confirm password',
      hasAccount: 'Already have an account?',
      loginHere: 'Login here',
    },
  },
  storyline: {
    kieuKyTag: 'Journey Chronicle',
    mainTitlePrefix: 'Heritage of',
    mainTitleSuffix: 'Vietnam',
    subTitle: 'Explore the heritage, culture, and hidden beauty of the S-shaped land. Each stop is a priceless story.',
    noteThangLong: 'Starting the journey from Thăng Long, a thousand years of civilization...',
    noteSea: 'Watch out for sea storms!',
    exploreBtn: 'Explore',
    lockedText: 'Locked',
    quests: {
      quest1: {
        title: 'Hoan Kiem Lake',
        desc: 'The heart of the capital, tied to the legend of the golden turtle returning the sacred sword. Walk around the lake and feel the slow pace of life.',
      },
      quest2: {
        title: 'Ha Long Bay',
        desc: 'A world natural wonder with thousands of magnificent limestone islands rising from the emerald green waters.',
      },
      quest3: {
        title: 'Hoi An Ancient Town',
        desc: 'A once busy trading port, now glittering in the light of silk lanterns and old mossy houses.',
      },
      quest4: {
        title: 'Imperial City of Hue',
        desc: 'A legacy of the old dynasty, where royal tombs and imperial court music take you back to heroic pages of history.',
      },
      quest5: {
        title: 'Mu Cang Chai',
        desc: 'Magnificent terraced rice fields weaving a golden silk carpet across the Northwest sky.',
      },
    },
    title: 'BeVietnam Storyline',
    subtitle: 'A cultural journey through Vietnam\'s heritage sites',
    difficulty: 'Difficulty',
    requirement: 'Requirement',
    completeTask: 'Complete Challenge',
    completed: '✓ Completed',
    locked: 'Locked',
    reset: 'Reset Journey',
    chapter: 'Chapter',
    done: 'Done',
    doing: 'In Progress',
    nextTask: 'Next Challenge',
    easy: 'Easy',
    medium: 'Medium',
    hard: 'Hard'
  },
  footer: {
    about: 'About BeVietnam',
    aboutDesc: 'A smart tourism platform that helps you discover Vietnam with cultural depth.',
    quickLinks: 'Quick Links',
    contact: 'Contact',
    copyright: '© 2026 BeVietnam. All rights reserved.',
    madeWith: 'Built with ❤️ in Vietnam',
  },
};

const translations: Record<Locale, Translations> = { vi, en };

export function getTranslation(locale: Locale, key: string): string {
  const keys = key.split('.');
  let result: string | Translations = translations[locale];

  for (const k of keys) {
    if (typeof result === 'object' && result !== null && k in result) {
      result = result[k];
    } else {
      return key; // Return key as fallback
    }
  }

  return typeof result === 'string' ? result : key;
}

export function t(locale: Locale, key: string): string {
  return getTranslation(locale, key);
}

export const defaultLocale: Locale = 'vi';
export const locales: Locale[] = ['vi', 'en'];
export const localeNames: Record<Locale, string> = {
  vi: 'Tiếng Việt',
  en: 'English',
};
