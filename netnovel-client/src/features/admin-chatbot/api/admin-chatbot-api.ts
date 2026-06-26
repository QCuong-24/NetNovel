import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type {
  ChatbotAdminSummary,
  ChatbotEmbeddingReindexResponse,
  ChatbotEmbeddingStatus,
  ChatbotFaq,
  ChatbotIntent,
} from '../types';

export async function getChatbotAdminSummary() {
  const response = await httpClient.get<ChatbotAdminSummary>(endpoints.adminChatbot.summary);

  return response.data;
}

export async function reloadChatbotKnowledge() {
  const response = await httpClient.post<ChatbotAdminSummary>(endpoints.adminChatbot.reload);

  return response.data;
}

export async function importDefaultChatbotKnowledge(replaceExisting: boolean) {
  const response = await httpClient.post<ChatbotAdminSummary>(
    `${endpoints.adminChatbot.importDefaults}?replaceExisting=${replaceExisting}`,
  );

  return response.data;
}

export async function reindexChatbotEmbeddings() {
  const response = await httpClient.post<ChatbotEmbeddingReindexResponse>(endpoints.adminChatbot.reindexEmbeddings);

  return response.data;
}

export async function getChatbotEmbeddingStatus() {
  const response = await httpClient.get<ChatbotEmbeddingStatus>(endpoints.adminChatbot.embeddingStatus);

  return response.data;
}

export async function getChatbotFaqs() {
  const response = await httpClient.get<ChatbotFaq[]>(endpoints.adminChatbot.faqs);

  return response.data;
}

export async function saveChatbotFaq(payload: ChatbotFaq) {
  const response = await httpClient.put<ChatbotFaq>(endpoints.adminChatbot.faq(payload.id), payload);

  return response.data;
}

export async function deleteChatbotFaq(id: string) {
  await httpClient.delete(endpoints.adminChatbot.faq(id));
}

export async function getChatbotIntents() {
  const response = await httpClient.get<ChatbotIntent[]>(endpoints.adminChatbot.intents);

  return response.data;
}

export async function saveChatbotIntent(payload: ChatbotIntent) {
  const response = await httpClient.put<ChatbotIntent>(endpoints.adminChatbot.intent(payload.id), payload);

  return response.data;
}

export async function deleteChatbotIntent(id: string) {
  await httpClient.delete(endpoints.adminChatbot.intent(id));
}
