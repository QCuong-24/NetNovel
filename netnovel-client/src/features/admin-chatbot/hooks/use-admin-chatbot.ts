import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  deleteChatbotFaq,
  deleteChatbotIntent,
  getChatbotAdminSummary,
  getChatbotEmbeddingStatus,
  getChatbotFaqs,
  getChatbotIntents,
  importDefaultChatbotKnowledge,
  reindexChatbotEmbeddings,
  reloadChatbotKnowledge,
  saveChatbotFaq,
  saveChatbotIntent,
} from '../api/admin-chatbot-api';

export function useChatbotAdminSummary() {
  return useQuery({
    queryKey: [...queryKeys.adminChatbot, 'summary'],
    queryFn: getChatbotAdminSummary,
  });
}

export function useChatbotFaqs() {
  return useQuery({
    queryKey: [...queryKeys.adminChatbot, 'faqs'],
    queryFn: getChatbotFaqs,
  });
}

export function useChatbotIntents() {
  return useQuery({
    queryKey: [...queryKeys.adminChatbot, 'intents'],
    queryFn: getChatbotIntents,
  });
}

export function useChatbotEmbeddingStatus() {
  return useQuery({
    queryKey: [...queryKeys.adminChatbot, 'embeddingStatus'],
    queryFn: getChatbotEmbeddingStatus,
  });
}

export function useSaveChatbotFaqMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: saveChatbotFaq,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('FAQ saved');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not save FAQ')),
  });
}

export function useDeleteChatbotFaqMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteChatbotFaq,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('FAQ deleted');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not delete FAQ')),
  });
}

export function useSaveChatbotIntentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: saveChatbotIntent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('Intent saved');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not save intent')),
  });
}

export function useDeleteChatbotIntentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteChatbotIntent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('Intent deleted');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not delete intent')),
  });
}

export function useReloadChatbotKnowledgeMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reloadChatbotKnowledge,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('Chatbot knowledge reloaded');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not reload chatbot knowledge')),
  });
}

export function useImportDefaultChatbotKnowledgeMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: importDefaultChatbotKnowledge,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.adminChatbot });
      toast.success('Default chatbot knowledge imported');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not import chatbot defaults')),
  });
}

export function useReindexChatbotEmbeddingsMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: reindexChatbotEmbeddings,
    onSuccess: (response) => {
      void queryClient.invalidateQueries({ queryKey: [...queryKeys.adminChatbot, 'embeddingStatus'] });
      toast.success(response.message);
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not reindex chatbot embeddings')),
  });
}
