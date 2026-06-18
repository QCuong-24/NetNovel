import { useTranslation } from 'react-i18next';
import type { ReaderSettings } from '../types';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

type ReaderSettingsPanelProps = {
  settings: ReaderSettings;
  onChange: <TKey extends keyof ReaderSettings>(key: TKey, value: ReaderSettings[TKey]) => void;
};

const fontOptions = ['sans', 'serif', 'mono'] as const;
const sizeOptions = ['sm', 'md', 'lg', 'xl'] as const;
const backgroundOptions = ['default', 'soft', 'sepia', 'dim'] as const;

export function ReaderSettingsPanel({ settings, onChange }: ReaderSettingsPanelProps) {
  const { t } = useTranslation();

  return (
    <div className="grid gap-4 rounded-lg border bg-card p-4 text-card-foreground shadow-sm">
      <div className="grid gap-2">
        <p className="text-sm font-semibold">{t('reader.font')}</p>
        <div className="flex flex-wrap gap-2">
          {fontOptions.map((font) => (
            <Button
              key={font}
              type="button"
              size="sm"
              variant={settings.fontFamily === font ? 'default' : 'outline'}
              onClick={() => onChange('fontFamily', font)}
            >
              {t(`reader.fonts.${font}`)}
            </Button>
          ))}
        </div>
      </div>

      <div className="grid gap-2">
        <p className="text-sm font-semibold">{t('reader.size')}</p>
        <div className="flex flex-wrap gap-2">
          {sizeOptions.map((size) => (
            <Button
              key={size}
              type="button"
              size="sm"
              variant={settings.fontSize === size ? 'default' : 'outline'}
              onClick={() => onChange('fontSize', size)}
            >
              {t(`reader.sizes.${size}`)}
            </Button>
          ))}
        </div>
      </div>

      <div className="grid gap-2">
        <p className="text-sm font-semibold">{t('reader.background')}</p>
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          {backgroundOptions.map((background) => (
            <button
              key={background}
              type="button"
              className={cn(
                'rounded-md border px-3 py-2 text-sm font-semibold transition-colors hover:border-primary',
                settings.background === background && 'border-primary ring-2 ring-ring',
              )}
              onClick={() => onChange('background', background)}
            >
              {t(`reader.backgrounds.${background}`)}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
