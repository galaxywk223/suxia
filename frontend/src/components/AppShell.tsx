import * as Tooltip from '@radix-ui/react-tooltip'
import { Download, FolderOpen, Images, ListChecks, Settings as SettingsIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import brandMark from '../assets/brand-mark.svg'
import type { PageId, Settings } from '../types'

const pages: Array<{ id: PageId; label: string; icon: typeof Download }> = [
  { id: 'download', label: '下载', icon: Download },
  { id: 'library', label: '媒体库', icon: Images },
  { id: 'tasks', label: '任务', icon: ListChecks },
]

interface AppShellProps {
  page: PageId
  title: string
  subtitle: string
  settings?: Settings
  onPageChange: (page: PageId) => void
  onOpenSettings: () => void
  children: ReactNode
}

export function AppShell({
  page,
  title,
  subtitle,
  settings,
  onPageChange,
  onOpenSettings,
  children,
}: AppShellProps) {
  const folderName = settings?.download_dir?.split(/[\\/]/).filter(Boolean).pop()
  return (
    <Tooltip.Provider delayDuration={500}>
      <div className="app-shell">
        <aside className="sidebar">
          <div className="brand" aria-label="Keepsake">
            <img className="brand-mark" src={brandMark} alt="" width={32} height={32} />
            <span className="brand-name">Keepsake</span>
          </div>
          <nav className="primary-nav" aria-label="主导航">
            {pages.map(({ id, label, icon: Icon }) => (
              <button
                className={`nav-item ${page === id ? 'is-active' : ''}`}
                key={id}
                onClick={() => onPageChange(id)}
                type="button"
                aria-current={page === id ? 'page' : undefined}
              >
                <Icon size={18} />
                <span>{label}</span>
              </button>
            ))}
          </nav>
          <div className="sidebar-footer">
            <Tooltip.Root>
              <Tooltip.Trigger asChild>
                <button className="status-card" onClick={onOpenSettings} type="button">
                  <span className="status-line">
                    <FolderOpen size={15} />
                    <strong>{folderName || '未设置下载目录'}</strong>
                  </span>
                  <span className="status-line">
                    <span className={`status-dot ${settings?.browser_profile_ready ? 'is-ready' : ''}`} aria-hidden="true" />
                    {settings?.browser_profile_ready ? '登录环境已配置' : '未配置登录环境'}
                  </span>
                </button>
              </Tooltip.Trigger>
              <Tooltip.Portal>
                <Tooltip.Content className="tooltip" side="right" sideOffset={10}>
                  {settings?.download_dir || '查看下载目录'}
                </Tooltip.Content>
              </Tooltip.Portal>
            </Tooltip.Root>
            <button className="nav-item" onClick={onOpenSettings} type="button">
              <SettingsIcon size={18} />
              <span>设置</span>
            </button>
          </div>
        </aside>

        <main className="main-area">
          <header className="page-header">
            <div>
              <h1>{title}</h1>
              <p>{subtitle}</p>
            </div>
            <button className="icon-button bordered mobile-settings" onClick={onOpenSettings} type="button" aria-label="打开设置">
              <SettingsIcon size={18} />
            </button>
          </header>
          <div className="page-content">{children}</div>
        </main>

        <nav className="mobile-nav" aria-label="移动端导航">
          {pages.map(({ id, label, icon: Icon }) => (
            <button
              className={page === id ? 'is-active' : ''}
              key={id}
              onClick={() => onPageChange(id)}
              type="button"
            >
              <Icon size={20} />
              <span>{label}</span>
            </button>
          ))}
        </nav>
      </div>
    </Tooltip.Provider>
  )
}
