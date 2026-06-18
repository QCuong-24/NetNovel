import { ImagePlus, Loader2, Upload } from 'lucide-react';
import type { ChangeEvent } from 'react';
import { useRef, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const acceptedImageTypes = ['image/jpeg', 'image/png', 'image/webp'];
const maxImageSize = 5 * 1024 * 1024;

type ImageUploaderProps = {
  title: string;
  description?: string;
  currentImageUrl?: string | null;
  buttonLabel?: string;
  emptyLabel?: string;
  className?: string;
  previewClassName?: string;
  disabled?: boolean;
  isUploading?: boolean;
  onUpload: (file: File) => Promise<unknown> | void;
};

export function ImageUploader({
  title,
  description,
  currentImageUrl,
  buttonLabel = 'Upload image',
  emptyLabel = 'No image yet',
  className,
  previewClassName,
  disabled = false,
  isUploading = false,
  onUpload,
}: ImageUploaderProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const displayUrl = previewUrl ?? currentImageUrl;

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    if (!acceptedImageTypes.includes(file.type)) {
      toast.error('Please choose a JPG, PNG, or WebP image');
      return;
    }

    if (file.size > maxImageSize) {
      toast.error('Image must be smaller than 5MB');
      return;
    }

    const objectUrl = URL.createObjectURL(file);
    setPreviewUrl(objectUrl);

    try {
      await onUpload(file);
    } finally {
      URL.revokeObjectURL(objectUrl);
      setPreviewUrl(null);
    }
  }

  return (
    <div className={cn('grid gap-3', className)}>
      <div className="grid gap-1">
        <p className="text-sm font-semibold">{title}</p>
        {description ? <p className="text-xs leading-5 text-muted-foreground">{description}</p> : null}
      </div>

      <div
        className={cn(
          'grid aspect-[3/4] place-items-center overflow-hidden rounded-lg border bg-muted text-muted-foreground',
          previewClassName,
        )}
      >
        {displayUrl ? (
          <img alt={title} className="h-full w-full object-cover" src={displayUrl} />
        ) : (
          <div className="grid justify-items-center gap-2 px-4 text-center text-sm font-semibold">
            <ImagePlus className="size-8" />
            <span>{emptyLabel}</span>
          </div>
        )}
      </div>

      <input
        ref={inputRef}
        accept={acceptedImageTypes.join(',')}
        className="sr-only"
        disabled={disabled || isUploading}
        type="file"
        onChange={handleFileChange}
      />
      <Button
        disabled={disabled || isUploading}
        type="button"
        variant="outline"
        onClick={() => inputRef.current?.click()}
      >
        {isUploading ? <Loader2 className="animate-spin" /> : <Upload />}
        {isUploading ? 'Uploading...' : buttonLabel}
      </Button>
    </div>
  );
}
