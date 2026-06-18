export type CloudinaryUploadSignature = {
  cloudName: string;
  apiKey: string;
  folder: string;
  timestamp: number;
  signature: string;
  uploadUrl: string;
};

export type ImageMetadata = {
  url: string;
  publicId: string;
};

export type CloudinaryUploadResponse = {
  secure_url: string;
  public_id: string;
};
