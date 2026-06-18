import { useTranslation } from 'react-i18next';
import { adPlacements } from '../lib/ad-config';
import type { AdSlotId } from '../types';
import { cn } from '@/lib/utils';

type AdSlotProps = {
  slot: AdSlotId;
  className?: string;
};

export function AdSlot({ slot, className }: AdSlotProps) {
  const { t } = useTranslation();
  const placement = adPlacements[slot];

  if (!placement.enabled) {
    return null;
  }

  return (
    <aside
      className={cn(
        'flex min-h-20 items-center justify-center rounded-lg border border-dashed bg-muted/50 px-4 py-3 text-center text-xs font-semibold uppercase tracking-wide text-muted-foreground',
        placement.format === 'rectangle' && 'min-h-64',
        className,
      )}
      data-ad-slot={slot}
    >
      {t('ads.placeholder')}
    </aside>
  );
}
