import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';

export function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const nextLanguage = i18n.language.startsWith('vi') ? 'en' : 'vi';

  return (
    <Button
      aria-label="Switch language"
      size="sm"
      variant="outline"
      type="button"
      onClick={() => void i18n.changeLanguage(nextLanguage)}
    >
      {i18n.language.startsWith('vi') ? 'VI' : 'EN'}
    </Button>
  );
}
