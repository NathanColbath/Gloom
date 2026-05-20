import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type MarkdownIt from 'markdown-it'
import container from 'markdown-it-container'

const studioImagesDir = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  '../../studio/images',
)

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function parseFileParam(info: string): string | null {
  const match = info.match(/file="([^"]+)"/)
  return match ? match[1] : null
}

/** Renders studio screenshot figures or dashed placeholders when PNGs are missing. */
export function studioScreenshotPlugin(md: MarkdownIt): void {
  md.use(container, 'studio-screenshot', {
    validate(params: string) {
      return !!parseFileParam(params.trim())
    },
    render(tokens, idx) {
      const token = tokens[idx]
      const file = parseFileParam(token.info.trim()) ?? ''
      const caption = token.content.trim()
      const exists = file.length > 0 && fs.existsSync(path.join(studioImagesDir, file))

      if (token.nesting === 1) {
        if (exists) {
          const alt = escapeHtml(caption || file)
          const cap = caption ? `<figcaption>${escapeHtml(caption)}</figcaption>` : ''
          return `<figure class="studio-screenshot"><img src="/studio/images/${encodeURI(file)}" alt="${alt}" loading="lazy" />${cap}`
        }
        const capHtml = caption
          ? `<p class="studio-screenshot-placeholder__caption">${escapeHtml(caption)}</p>`
          : ''
        return (
          `<div class="studio-screenshot-placeholder" aria-label="Screenshot placeholder">` +
          `<div class="studio-screenshot-placeholder__header">Screenshot: <code>${escapeHtml(file)}</code></div>` +
          capHtml +
          `<p class="studio-screenshot-placeholder__hint">Add <code>docs/studio/images/${escapeHtml(file)}</code></p>`
        )
      }
      return exists ? '</figure>' : '</div>'
    },
  })
}
