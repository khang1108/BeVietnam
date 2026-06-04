import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Khám phá địa điểm | Explore Places',
  description: 'Tìm những điểm đến tuyệt vời trên khắp Việt Nam - Find amazing destinations across Vietnam',
};

export default function ExploreLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
