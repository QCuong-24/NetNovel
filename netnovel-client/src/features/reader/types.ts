export type ReaderFontFamily = 'sans' | 'serif' | 'mono';
export type ReaderFontSize = 'sm' | 'md' | 'lg' | 'xl';
export type ReaderLineHeight = 'comfortable' | 'relaxed' | 'spacious';
export type ReaderWidth = 'narrow' | 'medium' | 'wide';
export type ReaderBackground = 'default' | 'soft' | 'sepia' | 'dim';

export type ReaderSettings = {
  fontFamily: ReaderFontFamily;
  fontSize: ReaderFontSize;
  lineHeight: ReaderLineHeight;
  width: ReaderWidth;
  background: ReaderBackground;
};
