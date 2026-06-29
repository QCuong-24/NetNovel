import { HardDrive, RefreshCw, Trash2, Volume2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { formatCount, formatDateTime } from '@/features/novels/lib/novel-format';
import {
  useAdminAudioAssets,
  useAdminAudioDashboard,
  useAdminAudioVoices,
  useCleanupExpiredAudioMutation,
  useDeleteAdminAudioAssetMutation,
  useKeepOnlyDefaultAdminAudioVoiceMutation,
  useRetryAdminAudioAssetMutation,
  useSyncAdminAudioVoicesMutation,
  useUpdateAdminAudioVoiceMutation,
} from '../hooks/use-admin-audio';
import type { AdminAudioAssetStatus, AdminAudioVoice } from '../types';

const statuses: Array<AdminAudioAssetStatus | ''> = ['', 'PROCESSING', 'READY', 'FAILED', 'CANCELLED', 'EXPIRED'];

function formatBytes(bytes?: number | null) {
  if (!bytes) {
    return '0 B';
  }

  const units = ['B', 'KB', 'MB', 'GB'];
  let value = bytes;
  let unitIndex = 0;
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024;
    unitIndex += 1;
  }

  return `${value.toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function formatExpiryDays(expiresAt?: string | null) {
  if (!expiresAt) {
    return 'No expiry';
  }

  const expiresTime = new Date(expiresAt).getTime();
  if (!Number.isFinite(expiresTime)) {
    return 'Invalid expiry';
  }

  const days = Math.ceil((expiresTime - Date.now()) / 86_400_000);
  if (days < 0) {
    return `${Math.abs(days)}d expired`;
  }

  if (days === 0) {
    return 'Expires today';
  }

  return `${days}d left`;
}

export function AudioManagerPanel() {
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState<AdminAudioAssetStatus | ''>('');
  const dashboardQuery = useAdminAudioDashboard();
  const assetsQuery = useAdminAudioAssets({ page, size: 10, status });
  const voicesQuery = useAdminAudioVoices();
  const retryMutation = useRetryAdminAudioAssetMutation();
  const deleteMutation = useDeleteAdminAudioAssetMutation();
  const cleanupMutation = useCleanupExpiredAudioMutation();
  const syncVoicesMutation = useSyncAdminAudioVoicesMutation();
  const keepOnlyDefaultMutation = useKeepOnlyDefaultAdminAudioVoiceMutation();
  const dashboard = dashboardQuery.data;
  const assetsPage = assetsQuery.data;

  return (
    <div className="grid gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-extrabold">Audio operations</h2>
          <p className="text-sm text-muted-foreground">Track generation jobs, storage usage, Polly voices, and cache health.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button disabled={cleanupMutation.isPending} type="button" variant="outline" onClick={() => cleanupMutation.mutate()}>
            <Trash2 />
            Cleanup expired
          </Button>
          <Button disabled={syncVoicesMutation.isPending} type="button" onClick={() => syncVoicesMutation.mutate()}>
            <RefreshCw className={syncVoicesMutation.isPending ? 'animate-spin' : undefined} />
            Sync voices
          </Button>
          <Button
            disabled={!voicesQuery.data?.length || keepOnlyDefaultMutation.isPending}
            type="button"
            variant="outline"
            onClick={() => keepOnlyDefaultMutation.mutate(voicesQuery.data ?? [])}
          >
            Keep only default
          </Button>
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Total assets" value={dashboard?.totalAssets ?? 0} />
        <MetricCard label="Ready / Failed" value={`${formatCount(dashboard?.readyAssets ?? 0)} / ${formatCount(dashboard?.failedAssets ?? 0)}`} />
        <MetricCard label="Storage used" value={formatBytes(dashboard?.totalStorageBytes)} />
        <MetricCard label="Characters today" value={dashboard?.providerCharactersToday ?? 0} />
        <MetricCard label="Generated today" value={dashboard?.generatedToday ?? 0} />
        <MetricCard label="Cache hits" value={dashboard?.cacheHitCount ?? 0} />
        <MetricCard label="Voices enabled" value={`${formatCount(dashboard?.enabledVoices ?? 0)} / ${formatCount(dashboard?.totalVoices ?? 0)}`} />
        <MetricCard label="Processing" value={dashboard?.processingAssets ?? 0} />
      </div>

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <Volume2 className="size-5 text-primary" />
            Audio assets
          </CardTitle>
          <select
            className="h-9 rounded-md border border-input bg-background px-3 text-sm font-semibold"
            value={status}
            onChange={(event) => {
              setStatus(event.target.value as AdminAudioAssetStatus | '');
              setPage(0);
            }}
          >
            {statuses.map((item) => (
              <option key={item || 'all'} value={item}>
                {item || 'ALL'}
              </option>
            ))}
          </select>
        </CardHeader>
        <CardContent className="grid gap-4">
          {assetsQuery.isLoading ? <p className="text-sm text-muted-foreground">Loading audio assets...</p> : null}
          {assetsPage?.content.length ? (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[960px] text-left text-sm">
                <thead className="border-b text-xs uppercase text-muted-foreground">
                  <tr>
                    <th className="p-2">Asset</th>
                    <th className="p-2">Chapter</th>
                    <th className="p-2">Voice</th>
                    <th className="p-2">Size</th>
                    <th className="p-2">Metrics</th>
                    <th className="p-2">Expiry</th>
                    <th className="p-2">Updated</th>
                    <th className="p-2">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {assetsPage.content.map((asset) => (
                    <tr className="border-b last:border-0" key={asset.assetId}>
                      <td className="p-2">
                        <div className="grid gap-1">
                          <span className="font-extrabold">#{asset.assetId}</span>
                          <Badge className={asset.status === 'FAILED' ? 'border-destructive text-destructive' : undefined} variant={asset.status === 'READY' ? 'secondary' : 'outline'}>
                            {asset.status}
                          </Badge>
                        </div>
                      </td>
                      <td className="p-2">
                        <div className="grid gap-1">
                          <span className="font-semibold">{asset.novelTitle}</span>
                          <span className="text-muted-foreground">#{asset.chapterId} {asset.chapterTitle}</span>
                        </div>
                      </td>
                      <td className="p-2">
                        {asset.voiceName}
                        <br />
                        <span className="text-muted-foreground">{asset.languageCode} {asset.engine}</span>
                      </td>
                      <td className="p-2">{formatBytes(asset.fileSizeBytes)}</td>
                      <td className="p-2 text-muted-foreground">
                        {formatCount(asset.providerCharacterCount ?? 0)} chars, {formatCount(asset.chunkCount ?? 0)} chunks
                        {asset.errorMessage ? <div className="max-w-xs truncate text-destructive">{asset.errorMessage}</div> : null}
                      </td>
                      <td className="p-2 text-muted-foreground">
                        <div>{formatExpiryDays(asset.expiresAt)}</div>
                        <div className="text-xs">{formatDateTime(asset.expiresAt ?? undefined)}</div>
                      </td>
                      <td className="p-2 text-muted-foreground">{formatDateTime(asset.updatedAt ?? undefined)}</td>
                      <td className="p-2">
                        <div className="flex gap-2">
                          <Button disabled={retryMutation.isPending} size="sm" type="button" variant="outline" onClick={() => retryMutation.mutate(String(asset.assetId))}>
                            Retry
                          </Button>
                          <Button disabled={deleteMutation.isPending} size="sm" type="button" variant="destructive" onClick={() => deleteMutation.mutate(String(asset.assetId))}>
                            Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : !assetsQuery.isLoading ? (
            <p className="text-sm text-muted-foreground">No audio assets found.</p>
          ) : null}
          <div className="flex items-center justify-between border-t pt-3">
            <span className="text-sm text-muted-foreground">Page {(assetsPage?.number ?? page) + 1} of {Math.max(assetsPage?.totalPages ?? 1, 1)}</span>
            <div className="flex gap-2">
              <Button disabled={assetsPage?.first ?? true} type="button" variant="outline" onClick={() => setPage((current) => Math.max(0, current - 1))}>Previous</Button>
              <Button disabled={assetsPage?.last ?? true} type="button" variant="outline" onClick={() => setPage((current) => current + 1)}>Next</Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <HardDrive className="size-5 text-primary" />
            Voice catalog
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-2">
          {voicesQuery.isLoading ? <p className="text-sm text-muted-foreground">Loading voices...</p> : null}
          {voicesQuery.data?.map((voice) => <VoiceCatalogRow key={voice.id} voice={voice} />)}
        </CardContent>
      </Card>
    </div>
  );
}

function VoiceCatalogRow({ voice }: { voice: AdminAudioVoice }) {
  const updateVoiceMutation = useUpdateAdminAudioVoiceMutation();
  const [enabled, setEnabled] = useState(voice.enabled);
  const [defaultVoice, setDefaultVoice] = useState(voice.defaultVoice);
  const [sortOrder, setSortOrder] = useState(String(voice.sortOrder ?? 0));
  const normalizedSortOrder = Number(sortOrder) || 0;
  const hasChanges =
    enabled !== voice.enabled ||
    defaultVoice !== voice.defaultVoice ||
    normalizedSortOrder !== (voice.sortOrder ?? 0);

  useEffect(() => {
    setEnabled(voice.enabled);
    setDefaultVoice(voice.defaultVoice);
    setSortOrder(String(voice.sortOrder ?? 0));
  }, [voice.defaultVoice, voice.enabled, voice.sortOrder]);

  return (
    <div className="grid gap-3 rounded-lg border p-3 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
      <div className="min-w-0">
        <p className="truncate font-extrabold">{voice.displayName}</p>
        <p className="text-sm text-muted-foreground">
          {voice.voiceName} - {voice.languageCode} - {voice.engine} - {voice.gender ?? 'unknown'}
        </p>
      </div>
      <div className="flex flex-wrap items-end gap-2">
        <label className="grid gap-1 text-xs font-semibold uppercase text-muted-foreground">
          Status
          <select
            className="h-9 rounded-md border bg-background px-2 text-sm font-medium normal-case text-foreground"
            value={enabled ? 'enabled' : 'disabled'}
            onChange={(event) => setEnabled(event.target.value === 'enabled')}
          >
            <option value="enabled">Enabled</option>
            <option value="disabled">Disabled</option>
          </select>
        </label>
        <label className="grid gap-1 text-xs font-semibold uppercase text-muted-foreground">
          Default
          <select
            className="h-9 rounded-md border bg-background px-2 text-sm font-medium normal-case text-foreground"
            value={defaultVoice ? 'yes' : 'no'}
            onChange={(event) => setDefaultVoice(event.target.value === 'yes')}
          >
            <option value="no">No</option>
            <option value="yes">Yes</option>
          </select>
        </label>
        <label className="grid w-20 gap-1 text-xs font-semibold uppercase text-muted-foreground">
          Sort
          <Input
            className="h-9"
            min={0}
            type="number"
            value={sortOrder}
            onChange={(event) => setSortOrder(event.target.value)}
          />
        </label>
        <Button
          disabled={!hasChanges || updateVoiceMutation.isPending}
          size="sm"
          type="button"
          onClick={() =>
            updateVoiceMutation.mutate({
              voiceId: String(voice.id),
              payload: { enabled, defaultVoice, sortOrder: normalizedSortOrder },
            })
          }
        >
          Save
        </Button>
      </div>
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: number | string }) {
  return (
    <Card>
      <CardContent className="grid gap-1 p-4">
        <p className="text-2xl font-extrabold">{typeof value === 'number' ? formatCount(value) : value}</p>
        <p className="text-xs font-semibold uppercase text-muted-foreground">{label}</p>
      </CardContent>
    </Card>
  );
}
