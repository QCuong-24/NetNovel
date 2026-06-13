import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type {
  CrawlChapterRecordListParams,
  CrawlChapterRecordPage,
  CrawlTask,
  CrawlTaskCreatePayload,
  CrawlTaskListParams,
  CrawlTaskPage,
} from '../types';

export async function getCrawlTasks(params: CrawlTaskListParams) {
  const searchParams = new URLSearchParams();

  if (params.status) {
    searchParams.set('status', params.status);
  }
  if (params.personal) {
    searchParams.set('personal', 'true');
  }
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 20));

  const response = await httpClient.get<CrawlTaskPage>(`${endpoints.crawlTasks.list}?${searchParams.toString()}`);

  return response.data;
}

export async function createCrawlTask(payload: CrawlTaskCreatePayload) {
  const response = await httpClient.post<CrawlTask>(endpoints.crawlTasks.create, payload);

  return response.data;
}

export async function getCrawlChapterRecords(params: CrawlChapterRecordListParams) {
  const searchParams = new URLSearchParams();

  if (params.status) {
    searchParams.set('status', params.status);
  }
  if (params.novelId) {
    searchParams.set('novelId', params.novelId);
  }
  if (params.start) {
    searchParams.set('start', params.start);
  }
  if (params.end) {
    searchParams.set('end', params.end);
  }
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 20));
  searchParams.set('sort', 'crawledAt,desc');

  const response = await httpClient.get<CrawlChapterRecordPage>(
    `${endpoints.crawlTasks.chapterRecords}?${searchParams.toString()}`,
  );

  return response.data;
}

export async function deleteCrawlChapterRecord(recordId: string) {
  await httpClient.delete(endpoints.crawlTasks.chapterRecord(recordId));
}
