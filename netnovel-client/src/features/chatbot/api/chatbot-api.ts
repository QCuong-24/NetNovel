import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { ChatbotRequest, ChatbotResponse } from '../types';

export async function sendChatbotMessage(payload: ChatbotRequest) {
  const response = await httpClient.post<ChatbotResponse>(endpoints.chatbot.message, payload);

  return response.data;
}
