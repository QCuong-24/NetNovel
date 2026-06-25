import type { Novel } from '@/features/novels/types';

export type ChatbotAction = {
  label: string;
  type: 'navigate' | string;
  value: string;
};

export type ChatbotRequest = {
  message: string;
  language?: 'vi' | 'en';
};

export type ChatbotResponse = {
  reply: string;
  language: 'vi' | 'en';
  intent: string;
  confidence: number;
  novels: Novel[];
  suggestedQuestions: string[];
  actions: ChatbotAction[];
};

export type ChatMessage =
  | {
      id: string;
      role: 'user';
      text: string;
    }
  | {
      id: string;
      role: 'bot';
      text: string;
      response?: ChatbotResponse;
    };
