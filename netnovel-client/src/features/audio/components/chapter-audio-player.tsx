import { Loader2, Volume2 } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { env } from '@/config/env';
import { useAudioVoices, useChapterAudioAsset, useCreateChapterAudioMutation } from '../hooks/use-chapter-audio';
import type { ChapterAudioResponse } from '../types';

type ChapterAudioPlayerProps = {
  chapterId?: string;
  onPlayingChange?: (isPlaying: boolean) => void;
};

export function ChapterAudioPlayer({ chapterId, onPlayingChange }: ChapterAudioPlayerProps) {
  const { t } = useTranslation();
  const audioRef = useRef<HTMLAudioElement>(null);
  const [audio, setAudio] = useState<ChapterAudioResponse | null>(null);
  const [voiceId, setVoiceId] = useState('');
  const voicesQuery = useAudioVoices();
  const audioMutation = useCreateChapterAudioMutation(chapterId);
  const assetQuery = useChapterAudioAsset(audio?.assetId ? String(audio.assetId) : undefined, audio?.status === 'PROCESSING');
  const selectedVoice = voicesQuery.data?.find((voice) => String(voice.id) === voiceId);
  const audioSrc = useMemo(() => {
    if (!audio?.audioUrl) {
      return null;
    }

    if (audio.audioUrl.startsWith('http')) {
      return audio.audioUrl;
    }

    const apiBaseUrl = env.apiBaseUrl.replace(/\/$/, '');
    const audioPath = audio.audioUrl.replace(/^\/api(?=\/)/, '').replace(/^\//, '');

    return `${apiBaseUrl}/${audioPath}`;
  }, [audio]);

  useEffect(() => {
    if (!voiceId && voicesQuery.data?.length) {
      const defaultVoice = voicesQuery.data.find((voice) => voice.defaultVoice) ?? voicesQuery.data[0];
      setVoiceId(String(defaultVoice.id));
    }
  }, [voiceId, voicesQuery.data]);

  useEffect(() => {
    if (assetQuery.data) {
      setAudio(assetQuery.data);
    }
  }, [assetQuery.data]);

  useEffect(() => {
    return () => {
      onPlayingChange?.(false);
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.removeAttribute('src');
        audioRef.current.load();
      }
    };
  }, [onPlayingChange]);

  async function handleCreateAudio() {
    if (!chapterId) {
      return;
    }

    const response = await audioMutation.mutateAsync(selectedVoice
      ? {
          languageCode: selectedVoice.languageCode,
          voiceName: selectedVoice.voiceName,
          engine: selectedVoice.engine,
          audioEncoding: 'MP3',
        }
      : undefined);
    setAudio(response);
  }

  return (
    <section className="mx-auto grid w-full max-w-2xl gap-3 rounded-lg border bg-background/80 p-3 text-left shadow-sm">
      <div className="flex flex-wrap items-center gap-2">
        {voicesQuery.data?.length ? (
          <select
            className="h-9 max-w-full rounded-md border border-input bg-background px-3 text-sm font-semibold"
            value={voiceId}
            onChange={(event) => setVoiceId(event.target.value)}
          >
            {voicesQuery.data.map((voice) => (
              <option key={voice.id} value={voice.id}>
                {voice.displayName} ({voice.languageCode}, {voice.engine})
              </option>
            ))}
          </select>
        ) : null}
        <Button disabled={!chapterId || audioMutation.isPending} size="sm" type="button" onClick={handleCreateAudio}>
          {audioMutation.isPending ? <Loader2 className="animate-spin" /> : <Volume2 />}
          {audioMutation.isPending ? t('audio.requesting', { defaultValue: 'Requesting audio...' }) : audioSrc ? t('audio.refresh') : t('audio.listen')}
        </Button>
        {audio?.cached ? <span className="text-xs font-semibold text-muted-foreground">{t('audio.cached')}</span> : null}
        {audio?.status === 'PROCESSING' ? <span className="text-xs font-semibold text-muted-foreground">{t('audio.generating')}</span> : null}
        {audio?.status === 'FAILED' ? <span className="text-xs font-semibold text-destructive">{audio.errorMessage ?? 'Audio failed'}</span> : null}
      </div>

      {audio?.status === 'READY' && audioSrc ? (
        <audio
          ref={audioRef}
          className="w-full"
          controls
          preload="metadata"
          src={audioSrc}
          onEnded={() => onPlayingChange?.(false)}
          onPause={() => onPlayingChange?.(false)}
          onPlay={() => onPlayingChange?.(true)}
        >
          <track kind="captions" />
        </audio>
      ) : null}
    </section>
  );
}
