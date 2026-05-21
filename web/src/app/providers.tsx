'use client';

import { ThemeProvider } from '@/hooks/useTheme';
import { I18nProvider } from '@/i18n';
import ScrollReveal from '@/components/ScrollReveal';

export default function ClientProviders({ children }: { children: React.ReactNode }) {
  return (
    <ThemeProvider>
      <I18nProvider>
        <ScrollReveal />
        {children}
      </I18nProvider>
    </ThemeProvider>
  );
}
