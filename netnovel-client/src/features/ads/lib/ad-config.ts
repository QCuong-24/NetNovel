import type { AdFormat, AdSlotId } from '../types';

type AdPlacement = {
  id: AdSlotId;
  format: AdFormat;
  enabled: boolean;
};

export const adPlacements: Record<AdSlotId, AdPlacement> = {
  home_top_banner: { id: 'home_top_banner', format: 'banner', enabled: true },
  home_between_sections: { id: 'home_between_sections', format: 'native', enabled: true },
  reader_top: { id: 'reader_top', format: 'banner', enabled: true },
  reader_after_chapter: { id: 'reader_after_chapter', format: 'banner', enabled: true },
  novel_detail_sidebar: { id: 'novel_detail_sidebar', format: 'rectangle', enabled: true },
};
