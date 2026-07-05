import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import type { ChapterContent } from '../types';

import { ChapterForm } from './chapter-form';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}));

describe('ChapterForm', () => {
  // Form contract: validate user input, then emit the ChapterPayload expected by chapter mutations.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('submits create values as a chapter payload', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<ChapterForm mode="create" onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText('chapterForm.title'), { target: { value: 'Opening Gate' } });
    fireEvent.change(screen.getByLabelText('chapterForm.number'), { target: { value: '3' } });
    fireEvent.change(screen.getByPlaceholderText('chapterForm.contentPlaceholder'), {
      target: { value: 'This chapter content is long enough for validation.' },
    });
    await user.click(screen.getByRole('button', { name: 'chapterForm.createChapter' }));

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'Opening Gate',
      chapterNumber: 3,
      content: 'This chapter content is long enough for validation.',
    });
  });

  it('shows validation errors and does not submit invalid values', async () => {
    const onSubmit = vi.fn();

    render(<ChapterForm mode="create" onSubmit={onSubmit} />);

    fireEvent.click(screen.getByRole('button', { name: 'chapterForm.createChapter' }));

    expect(await screen.findByText('chapterForm.titleError')).toBeInTheDocument();
    expect(screen.getByText('chapterForm.contentError')).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('renders edit values and submits updated content', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    const chapter = chapterContent();

    render(<ChapterForm chapter={chapter} mode="edit" onSubmit={onSubmit} />);

    expect(screen.getByDisplayValue(chapter.title)).toBeInTheDocument();
    expect(screen.getByDisplayValue(String(chapter.chapterNumber))).toBeInTheDocument();
    expect(screen.getByDisplayValue(chapter.content)).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('chapterForm.title'), { target: { value: 'Updated Chapter' } });
    await user.click(screen.getByRole('button', { name: 'chapterForm.saveChanges' }));

    expect(onSubmit).toHaveBeenCalledWith({
      title: 'Updated Chapter',
      chapterNumber: chapter.chapterNumber,
      content: chapter.content,
    });
  });

  it('calls onCancel when the cancel button is clicked', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(<ChapterForm mode="create" onCancel={onCancel} onSubmit={vi.fn()} />);

    await user.click(screen.getByRole('button', { name: 'chapterForm.cancel' }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('disables actions and shows saving text while submitting', () => {
    render(<ChapterForm mode="edit" isSubmitting onCancel={vi.fn()} onSubmit={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'chapterForm.saving' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'chapterForm.cancel' })).toBeDisabled();
  });

  function chapterContent(): ChapterContent {
    return {
      chapterId: 1,
      novelId: 10,
      novelTitle: 'NetNovel Sample',
      title: 'Original Chapter',
      chapterNumber: 2,
      content: 'Existing chapter content is long enough for validation.',
    };
  }
});
