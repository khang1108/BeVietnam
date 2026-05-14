import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Sự kiện | Events',
  description: 'Các sự kiện đang diễn ra và sắp tới tại Việt Nam - Ongoing and upcoming events in Vietnam',
};

export default function EventsLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
