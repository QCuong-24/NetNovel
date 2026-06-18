import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  getNovelCoverUploadSignature,
  getUserAvatarUploadSignature,
  updateNovelCover,
  updateUserAvatar,
  uploadImageToCloudinary,
} from '../api/upload-api';

export function useUploadNovelCoverMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (file: File) => {
      const signature = await getNovelCoverUploadSignature(novelId);
      const metadata = await uploadImageToCloudinary(file, signature);

      return updateNovelCover(novelId, metadata);
    },
    onSuccess: (novel) => {
      queryClient.setQueryData([...queryKeys.novels, novelId], novel);
      queryClient.invalidateQueries({ queryKey: queryKeys.novels });
      toast.success('Cover image updated');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not upload cover image'));
    },
  });
}

export function useUploadUserAvatarMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (file: File) => {
      const signature = await getUserAvatarUploadSignature();
      const metadata = await uploadImageToCloudinary(file, signature);

      return updateUserAvatar(metadata);
    },
    onSuccess: (user) => {
      queryClient.setQueryData(queryKeys.auth, user);
      toast.success('Avatar updated');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not upload avatar'));
    },
  });
}
