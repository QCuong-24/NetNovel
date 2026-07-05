import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { Novel } from '../types';

import { NovelForm } from './novel-form';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

vi.mock('../hooks/use-novels', () => ({
  useGenres: vi.fn(),
  useNovelTags: vi.fn(),
  useTags: vi.fn(),
}));

vi.mock('@/features/uploads/hooks/use-image-upload', () => ({
  useUploadNovelCoverMutation: vi.fn(),
}));

import { useUploadNovelCoverMutation } from '@/features/uploads/hooks/use-image-upload';
import { useGenres, useNovelTags, useTags } from '../hooks/use-novels';

const mockedUseGenres = vi.mocked(useGenres);
const mockedUseTags = vi.mocked(useTags);
const mockedUseNovelTags = vi.mocked(useNovelTags);
const mockedUseUploadNovelCoverMutation = vi.mocked(useUploadNovelCoverMutation);

describe('NovelForm', () => {
  // Form contract: load selectable metadata, validate user input, then emit the NovelPayload used by mutations.

  beforeEach(() => {
    vi.clearAllMocks();
    mockedUseGenres.mockReturnValue({
      data: [
        { genreId: 1, name: 'Fantasy' },
        { genreId: 2, name: 'Romance' },
      ],
      isLoading: false,
    } as ReturnType<typeof useGenres>);
    mockedUseTags.mockReturnValue({
      data: [
        { tagId: 1, name: 'Cultivation' },
        { tagId: 2, name: 'Adventure' },
      ],
      isLoading: false,
    } as ReturnType<typeof useTags>);
    mockedUseNovelTags.mockReturnValue({ data: undefined } as ReturnType<typeof useNovelTags>);
    mockedUseUploadNovelCoverMutation.mockReturnValue({
      isPending: false,
      mutateAsync: vi.fn(),
    } as unknown as ReturnType<typeof useUploadNovelCoverMutation>);
  });

  it('submits create values with selected genres and tags', async () => {
    const onSubmit = vi.fn();

    render(<NovelForm mode="create" onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText('novelForm.title'), { target: { value: 'Lord of Tests' } });
    fireEvent.change(screen.getByLabelText('novelForm.author'), { target: { value: 'Test Author' } });
    fireEvent.change(screen.getByLabelText('novelForm.description'), {
      target: { value: 'A long enough description.' },
    });
    fireEvent.change(screen.getByLabelText('novelForm.coverImageUrl'), {
      target: { value: 'https://example.com/cover.png' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Fantasy' }));
    fireEvent.click(screen.getByRole('button', { name: 'Cultivation' }));
    fireEvent.change(screen.getByLabelText('novelForm.status'), { target: { value: 'COMPLETED' } });
    fireEvent.change(screen.getByLabelText('novelForm.accessStatus'), { target: { value: 'PREVIEW_ONLY' } });
    fireEvent.click(screen.getByRole('button', { name: 'novelForm.createNovel' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith({
      title: 'Lord of Tests',
      author: 'Test Author',
      description: 'A long enough description.',
      coverImageUrl: 'https://example.com/cover.png',
      genres: ['Fantasy'],
      tags: ['Cultivation'],
      status: 'COMPLETED',
      accessStatus: 'PREVIEW_ONLY',
    }));
  });

  it('shows validation errors and does not submit invalid values', async () => {
    const onSubmit = vi.fn();

    render(<NovelForm mode="create" onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText('novelForm.coverImageUrl'), { target: { value: 'not-a-url' } });
    fireEvent.click(screen.getByRole('button', { name: 'novelForm.createNovel' }));

    expect(await screen.findByText('novelForm.authorError')).toBeInTheDocument();
    expect(screen.getByText('novelForm.descriptionError')).toBeInTheDocument();
    expect(screen.getByText('novelForm.coverUrlError')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('renders edit values, existing novel tags, and submits updated values', async () => {
    const onSubmit = vi.fn();
    const novel = existingNovel();
    mockedUseNovelTags.mockReturnValue({
      data: [{ tagId: 1, name: 'Cultivation' }],
    } as ReturnType<typeof useNovelTags>);

    render(<NovelForm novel={novel} mode="edit" onSubmit={onSubmit} />);

    expect(screen.getByDisplayValue(novel.title)).toBeInTheDocument();
    expect(screen.getByDisplayValue(novel.author)).toBeInTheDocument();
    expect(screen.getAllByText('Cultivation').length).toBeGreaterThanOrEqual(2);

    fireEvent.change(screen.getByLabelText('novelForm.title'), { target: { value: 'Updated Novel' } });
    fireEvent.click(screen.getByRole('button', { name: 'Adventure' }));
    fireEvent.click(screen.getByRole('button', { name: 'novelForm.saveChanges' }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith({
      title: 'Updated Novel',
      author: novel.author,
      description: novel.description,
      coverImageUrl: novel.coverImageUrl,
      genres: novel.genres,
      tags: ['Cultivation', 'Adventure'],
      status: novel.status,
      accessStatus: novel.accessStatus,
    }));
  });

  it('shows loading copy while genre and tag options are loading', () => {
    mockedUseGenres.mockReturnValue({ data: undefined, isLoading: true } as ReturnType<typeof useGenres>);
    mockedUseTags.mockReturnValue({ data: undefined, isLoading: true } as ReturnType<typeof useTags>);

    render(<NovelForm mode="create" onSubmit={vi.fn()} />);

    expect(screen.getByText('novelForm.loadingGenres')).toBeInTheDocument();
    expect(screen.getByText('novelForm.loadingTags')).toBeInTheDocument();
  });

  it('calls onCancel and disables actions while submitting', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(<NovelForm mode="edit" isSubmitting onCancel={onCancel} onSubmit={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'novelForm.saving' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'novelForm.cancel' })).toBeDisabled();

    render(<NovelForm mode="create" onCancel={onCancel} onSubmit={vi.fn()} />);
    const activeForm = screen.getAllByRole('button', { name: 'novelForm.cancel' }).at(-1);

    if (!activeForm) {
      throw new Error('Cancel button was not rendered');
    }

    await user.click(activeForm);
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('shows create-mode cover preview guidance instead of upload controls', () => {
    render(<NovelForm mode="create" onSubmit={vi.fn()} />);

    expect(screen.getByText('novelForm.uploadAfterCreate')).toBeInTheDocument();
    expect(screen.queryByText('novelForm.uploadCover')).not.toBeInTheDocument();
  });

  it('renders upload controls in edit mode', () => {
    render(<NovelForm novel={existingNovel()} mode="edit" onSubmit={vi.fn()} />);

    expect(screen.getByText('novelForm.cloudinaryCover')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /novelForm.uploadCover/ })).toBeInTheDocument();
  });

  function existingNovel(): Novel {
    return {
      novelId: 10,
      title: 'Original Novel',
      author: 'Original Author',
      description: 'Original description long enough.',
      coverImageUrl: 'https://example.com/original.png',
      coverImagePublicId: 'original-cover',
      views: 0,
      follows: 0,
      likes: 0,
      bookmarks: 0,
      genres: ['Fantasy'],
      status: 'ONGOING',
      accessStatus: 'NORMAL',
      chapterCount: 3,
    };
  }
});
