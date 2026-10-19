import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { RefreshCw } from 'lucide-react'
import { useMemo } from 'react'
import { api } from '../api'
import type { Job } from '../types'
import { JobList } from './JobList'

const groups: Array<{ id: string; label: string; statuses: string[] }> = [
  { id: 'active', label: '进行中', statuses: ['queued', 'resolving', 'downloading'] },
  { id: 'failed', label: '需要处理', statuses: ['failed', 'interrupted', 'cancelled'] },
  { id: 'completed', label: '已完成', statuses: ['completed'] },
]

export function TasksPage() {
  const queryClient = useQueryClient()
  const jobs = useQuery({ queryKey: ['jobs'], queryFn: api.jobs })
  const cancel = useMutation({ mutationFn: api.cancelJob, onSuccess: () => queryClient.invalidateQueries({ queryKey: ['jobs'] }) })
  const retry = useMutation({ mutationFn: api.retryJob, onSuccess: () => queryClient.invalidateQueries({ queryKey: ['jobs'] }) })
  const grouped = useMemo(() => {
    const all = jobs.data ?? []
    return groups.map((group) => ({ ...group, jobs: all.filter((job: Job) => group.statuses.includes(job.status)) }))
  }, [jobs.data])
  const total = jobs.data?.length ?? 0

  return (
    <section className="tasks-page">
      <div className="tasks-toolbar">
        <div className="task-stats">
          {grouped.map((group) => (
            <span className="task-stat" key={group.id}><strong>{group.jobs.length}</strong>{group.label}</span>
          ))}
        </div>
        <button className="icon-button bordered" onClick={() => jobs.refetch()} type="button" aria-label="刷新任务">
          <RefreshCw size={17} className={jobs.isFetching ? 'spin' : ''} />
        </button>
      </div>
      {jobs.error ? <p className="inline-error">{jobs.error.message}</p> : null}
      {total ? grouped.filter((group) => group.jobs.length).map((group) => (
        <div className="task-group" key={group.id}>
          <h2 className="eyebrow">{group.label} · {group.jobs.length}</h2>
          <JobList
            jobs={group.jobs}
            onCancel={(id) => cancel.mutate(id)}
            onRetry={(id) => retry.mutate(id)}
          />
        </div>
      )) : (
        <div className="card">
          <JobList jobs={[]} onCancel={() => undefined} onRetry={() => undefined} />
        </div>
      )}
    </section>
  )
}
