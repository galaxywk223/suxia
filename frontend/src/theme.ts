export type ThemePreference = 'system' | 'dark' | 'light'

const storageKey = 'keepsake.theme'

export function readThemePreference(): ThemePreference {
  try {
    const value = window.localStorage.getItem(storageKey)
    if (value === 'system' || value === 'dark' || value === 'light') return value
  } catch {
    // Storage can be unavailable; fall back to the default theme.
  }
  return 'dark'
}

export function applyThemePreference(preference: ThemePreference) {
  const root = document.documentElement
  if (preference === 'system') delete root.dataset.theme
  else root.dataset.theme = preference
}

export function saveThemePreference(preference: ThemePreference) {
  applyThemePreference(preference)
  try {
    window.localStorage.setItem(storageKey, preference)
  } catch {
    // The choice still applies for this session.
  }
}
