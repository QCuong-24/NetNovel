import { useCallback, useLayoutEffect, useRef, type TextareaHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

type AutoGrowTextareaProps = TextareaHTMLAttributes<HTMLTextAreaElement>;

export function AutoGrowTextarea({ className, value, onChange, onInput, ...props }: AutoGrowTextareaProps) {
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  const resize = useCallback((textarea = textareaRef.current) => {
    if (!textarea) {
      return;
    }

    if (typeof window === 'undefined') {
      return;
    }

    textarea.style.height = 'auto';
    textarea.style.height = `${Math.max(textarea.scrollHeight + 2, Math.round(window.innerHeight * 0.65))}px`;
  }, []);

  const scheduleResize = useCallback((textarea = textareaRef.current) => {
    if (!textarea || typeof window === 'undefined') {
      return;
    }

    window.requestAnimationFrame(() => {
      resize(textarea);
      window.setTimeout(() => resize(textarea), 0);
    });
  }, [resize]);

  useLayoutEffect(() => {
    scheduleResize();
  }, [scheduleResize, value]);

  return (
    <textarea
      ref={(element) => {
        textareaRef.current = element;

        if (element) {
          scheduleResize(element);
        }
      }}
      className={cn(
        'min-h-[65vh] w-full resize-none overflow-hidden rounded-md border bg-background px-4 py-4 text-base leading-8 text-foreground shadow-sm outline-none focus-visible:ring-2 focus-visible:ring-ring',
        className,
      )}
      value={value}
      style={{
        height: '65vh',
        overflow: 'hidden',
      }}
      {...props}
      onInput={(event) => {
        onInput?.(event);
        resize(event.currentTarget);
      }}
      onChange={(event) => {
        onChange?.(event);
        resize(event.currentTarget);
      }}
    />
  );
}
