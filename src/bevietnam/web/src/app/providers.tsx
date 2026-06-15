'use client';

import { ThemeProvider } from '@/hooks/useTheme';
import { AuthProvider } from '@/hooks/useAuth';
import { I18nProvider } from '@/i18n';
import ScrollReveal from '@/components/ScrollReveal';

export default function ClientProviders({ children }: { children: React.ReactNode }) {
  return (
    <ThemeProvider>
      <I18nProvider>
        <AuthProvider>
          <ScrollReveal />
          {children}
        </AuthProvider>
      </I18nProvider>
    </ThemeProvider>
  );
}
