import { FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Bot, Loader2, MessageCircle, Send, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { sendChatbotMessage } from '../api/chatbot-api';
import type { ChatMessage, ChatbotResponse } from '../types';

const initialSuggestions = ['Truyện hot', 'Truyện hoàn thành', 'Latest updates', 'How to follow a novel?'];

export function ChatbotWidget() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: user, isLoading: isLoadingUser } = useCurrentUser();
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: crypto.randomUUID(),
      role: 'bot',
      text: 'Hello! I can help you find novels or answer FAQ questions in English/Vietnamese.',
    },
  ]);
  const [isSending, setIsSending] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const latestResponse = useMemo(() => {
    const latestBotMessage = [...messages]
      .reverse()
      .find((message): message is Extract<ChatMessage, { role: 'bot' }> => message.role === 'bot' && Boolean(message.response));

    return latestBotMessage?.response;
  }, [messages]);

  useEffect(() => {
    if (!open) {
      return;
    }

    window.setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }, 0);
  }, [isSending, messages, open]);

  async function sendMessage(message: string) {
    const trimmed = message.trim();
    if (!trimmed || isSending || !user) {
      return;
    }

    setInput('');
    setIsSending(true);
    setMessages((current) => [
      ...current,
      { id: crypto.randomUUID(), role: 'user', text: trimmed },
    ]);

    try {
      const response = await sendChatbotMessage({ message: trimmed });
      setMessages((current) => [
        ...current,
        { id: crypto.randomUUID(), role: 'bot', text: response.reply, response },
      ]);
    } catch {
      setMessages((current) => [
        ...current,
        {
          id: crypto.randomUUID(),
          role: 'bot',
          text: 'Mình đang không kết nối được chatbot. Bạn thử lại sau một chút nhé.',
        },
      ]);
    } finally {
      setIsSending(false);
      window.setTimeout(() => inputRef.current?.focus(), 0);
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void sendMessage(input);
  }

  function handleAction(action: ChatbotResponse['actions'][number]) {
    if (action.type === 'navigate') {
      navigate(action.value);
      setOpen(false);
    }
  }

  return (
    <div className="fixed bottom-4 right-4 z-50">
      {open ? (
        <Card className="flex h-[min(620px,calc(100vh-2rem))] w-[min(380px,calc(100vw-2rem))] flex-col overflow-hidden shadow-2xl">
          <CardHeader className="flex-row items-center justify-between space-y-0 border-b px-4 py-3">
            <CardTitle className="flex items-center gap-2 text-base">
              <Bot className="size-5 text-primary" />
              NetNovel Assistant
            </CardTitle>
            <Button aria-label="Close chatbot" size="icon" variant="ghost" onClick={() => setOpen(false)}>
              <X className="size-4" />
            </Button>
          </CardHeader>

          <CardContent className="flex min-h-0 flex-1 flex-col gap-3 p-3">
            {!user && !isLoadingUser ? (
              <div className="grid flex-1 place-items-center rounded-2xl border border-dashed p-4 text-center">
                <div className="grid gap-3">
                  <Bot className="mx-auto size-10 text-primary" />
                  <div className="grid gap-1">
                    <p className="text-sm font-bold">Bạn cần đăng nhập để dùng chatbot.</p>
                    <p className="text-xs leading-5 text-muted-foreground">
                      Chatbot sẽ lưu các câu hỏi chưa hiểu để cải thiện FAQ/intent, nên mình chỉ bật cho tài khoản đã đăng nhập.
                    </p>
                  </div>
                  <Button type="button" onClick={() => navigate(routes.login)}>
                    Đăng nhập
                  </Button>
                </div>
              </div>
            ) : (
              <>
                <div className="min-h-0 flex-1 space-y-3 overflow-y-auto pr-1">
                  {messages.map((message) => (
                    <div
                      key={message.id}
                      className={cn(
                        'max-w-[88%] rounded-2xl px-3 py-2 text-sm leading-6',
                        message.role === 'user'
                          ? 'ml-auto bg-primary text-primary-foreground'
                          : 'bg-muted text-foreground',
                      )}
                    >
                      <p>{message.text}</p>
                      {message.role === 'bot' && message.response ? <BotResponseDetails response={message.response} /> : null}
                    </div>
                  ))}
                  {isSending || isLoadingUser ? (
                    <div className="inline-flex items-center gap-2 rounded-2xl bg-muted px-3 py-2 text-sm text-muted-foreground">
                      <Loader2 className="size-4 animate-spin" />
                      Thinking...
                    </div>
                  ) : null}
                  <div ref={messagesEndRef} />
                </div>

                <div className="flex flex-wrap gap-2">
                  {(latestResponse?.suggestedQuestions ?? initialSuggestions).slice(0, 4).map((suggestion) => (
                    <Button
                      key={suggestion}
                      className="h-8 rounded-full px-3 text-xs"
                      disabled={isSending || isLoadingUser}
                      type="button"
                      variant="outline"
                      onClick={() => void sendMessage(suggestion)}
                    >
                      {suggestion}
                    </Button>
                  ))}
                </div>

                <form className="flex gap-2" onSubmit={handleSubmit}>
                  <Input
                    ref={inputRef}
                    value={input}
                    placeholder={t('chatbot.placeholder')}
                    onChange={(event) => setInput(event.target.value)}
                  />
                  <Button disabled={isSending || isLoadingUser || !input.trim()} size="icon" type="submit">
                    <Send className="size-4" />
                  </Button>
                </form>
              </>
            )}
          </CardContent>
        </Card>
      ) : (
        <Button
          className="h-14 w-14 rounded-full shadow-xl"
          aria-label="Open chatbot"
          size="icon"
          onClick={() => setOpen(true)}
        >
          <MessageCircle className="size-6" />
        </Button>
      )}
    </div>
  );

  function BotResponseDetails({ response }: { response: ChatbotResponse }) {
    return (
      <div className="mt-3 space-y-2">
        {response.novels?.length ? (
          <div className="space-y-2">
            {response.novels.map((novel) => (
              <Link
                key={novel.novelId}
                className="block rounded-xl border bg-background p-2 text-foreground transition hover:border-primary"
                to={`/novels/${novel.novelId}`}
                onClick={() => setOpen(false)}
              >
                <div className="line-clamp-1 text-sm font-bold">{novel.title}</div>
                <div className="line-clamp-1 text-xs text-muted-foreground">{novel.author}</div>
                <div className="mt-1 flex flex-wrap gap-1">
                  <Badge className="text-[10px]" variant="secondary">
                    {novel.status}
                  </Badge>
                  {novel.genres?.slice(0, 2).map((genre) => (
                    <Badge key={genre} className="text-[10px]" variant="outline">
                      {genre}
                    </Badge>
                  ))}
                </div>
              </Link>
            ))}
          </div>
        ) : null}

        {response.actions?.length ? (
          <div className="flex flex-wrap gap-2">
            {response.actions.map((action) => (
              <Button
                key={`${action.type}-${action.value}`}
                className="h-8 rounded-full border-primary/30 bg-background px-3 text-xs text-primary shadow-sm transition-colors hover:border-primary hover:bg-primary/10 hover:text-primary hover:underline"
                type="button"
                variant="outline"
                onClick={() => handleAction(action)}
              >
                {action.label}
              </Button>
            ))}
          </div>
        ) : null}
      </div>
    );
  }
}
