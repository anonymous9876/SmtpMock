import { Component, Input, OnChanges, SimpleChanges, ViewEncapsulation } from '@angular/core';
import DOMPurify from 'dompurify';

@Component({
  selector: 'app-sanitized-html',
  standalone: true,
  template: `
    <div class="content" [innerHTML]="sanitizedHtml"></div>
  `,
  encapsulation: ViewEncapsulation.ShadowDom,
  styles: [`
    :host {
      display: block;
      font-family: inherit;
      color: inherit;
      line-height: inherit;
    }

    .content {
      all: initial;
      font-family: inherit;
      color: inherit;
      line-height: inherit;
    }

    .content * {
      all: revert;
    }
  `]
})
export class SanitizedHtmlComponent implements OnChanges {
  @Input() html: string | null | undefined;

  sanitizedHtml = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['html']) {
      this.sanitizedHtml = this.sanitizeHtml(this.html);
    }
  }

  private sanitizeHtml(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return DOMPurify.sanitize(value, {
      FORBID_TAGS: ['script']
    });
  }
}
