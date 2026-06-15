import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Đóng góp cộng đồng | Community Contributions',
  description: 'Chia sẻ những địa điểm, món ăn yêu thích của bạn - Share your favorite places and food discoveries',
};

export default function ContributeLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
