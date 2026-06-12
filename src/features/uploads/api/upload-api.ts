import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { User } from '@/features/auth/types';
import type { Novel } from '@/features/novels/types';
import type { CloudinaryUploadResponse, CloudinaryUploadSignature, ImageMetadata } from '../types';

export async function getNovelCoverUploadSignature(novelId: string) {
  const response = await httpClient.get<CloudinaryUploadSignature>(
    endpoints.novels.coverSignature(novelId),
  );

  return response.data;
}

export async function updateNovelCover(novelId: string, metadata: ImageMetadata) {
  const response = await httpClient.patch<Novel>(endpoints.novels.cover(novelId), metadata);

  return response.data;
}

export async function getUserAvatarUploadSignature() {
  const response = await httpClient.get<CloudinaryUploadSignature>(endpoints.users.avatarSignature);

  return response.data;
}

export async function updateUserAvatar(metadata: ImageMetadata) {
  const response = await httpClient.patch<User>(endpoints.users.avatar, metadata);

  return response.data;
}

export async function uploadImageToCloudinary(file: File, signature: CloudinaryUploadSignature) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('api_key', signature.apiKey);
  formData.append('timestamp', String(signature.timestamp));
  formData.append('signature', signature.signature);
  formData.append('folder', signature.folder);

  const response = await fetch(signature.uploadUrl, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const message =
      typeof errorBody?.error?.message === 'string'
        ? errorBody.error.message
        : 'Could not upload image to Cloudinary';

    throw new Error(message);
  }

  const data = (await response.json()) as CloudinaryUploadResponse;

  return {
    url: data.secure_url,
    publicId: data.public_id,
  };
}
