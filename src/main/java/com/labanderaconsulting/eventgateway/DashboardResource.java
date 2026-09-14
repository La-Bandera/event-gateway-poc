package com.labanderaconsulting.eventgateway;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Serves the live event dashboard: a single static page that polls {@link
 * EventsResource} and renders the Kafka round-trip as it happens. Kept as
 * one inline HTML/JS string rather than pulling in a templating dependency —
 * this is a PoC page, not an application.
 */
@Path("/events")
@Tag(name = "Dashboard", description = "Live view of events flowing through Kafka")
public class DashboardResource {

    private static final String PAGE = """
            <!doctype html>
            <html lang="en">
            <head>
            <meta charset="utf-8">
            <title>event-gateway-poc — live events</title>
            <style>
              body { font-family: -apple-system, system-ui, sans-serif; margin: 2rem; background: #0f1115; color: #e6e6e6; }
              h1 { font-size: 1.25rem; }
              h2 { font-size: 1rem; color: #9aa4b2; margin-top: 2rem; }
              table { width: 100%; border-collapse: collapse; margin-top: 0.5rem; }
              th, td { text-align: left; padding: 0.4rem 0.6rem; border-bottom: 1px solid #262a33; font-size: 0.85rem; }
              th { color: #9aa4b2; font-weight: 600; }
              td.mono { font-family: ui-monospace, monospace; }
              .empty { color: #6b7280; font-style: italic; padding: 0.75rem 0; }
              .badge { display: inline-block; padding: 0.1rem 0.5rem; border-radius: 999px; font-size: 0.75rem; }
              .badge.processed { background: #14532d; color: #86efac; }
              .badge.rejected { background: #450a0a; color: #fca5a5; }
              a { color: #7dd3fc; }
            </style>
            </head>
            <body>
              <h1>event-gateway-poc — live Kafka events</h1>
              <p>Polls <code>GET /events/recent</code> every 3s. Not persisted — a pod restart clears it.
                 <a href="/q/swagger-ui">API docs</a> · <a href="/status">status</a></p>

              <h2>Raw (ingested via <code>POST /ingest</code>)</h2>
              <table id="raw"><thead><tr><th>Time</th><th>id</th><th>source</th><th>type</th></tr></thead>
                <tbody><tr><td colspan="4" class="empty">No events yet — POST to /ingest to see one here.</td></tr></tbody>
              </table>

              <h2>Processed (after the Camel route + transform)</h2>
              <table id="processed"><thead><tr><th>Time</th><th>id</th><th>status</th><th>processedBy</th></tr></thead>
                <tbody><tr><td colspan="4" class="empty">No events yet.</td></tr></tbody>
              </table>

            <script>
              function row(cells) {
                return '<tr>' + cells.map(c => '<td' + (c.mono ? ' class="mono"' : '') + '>' + c.text + '</td>').join('') + '</tr>';
              }
              function badge(status) {
                const cls = status === 'PROCESSED' ? 'processed' : 'rejected';
                return '<span class="badge ' + cls + '">' + status + '</span>';
              }
              async function refresh() {
                try {
                  const res = await fetch('/events/recent');
                  if (!res.ok) return;
                  const data = await res.json();

                  const rawBody = document.querySelector('#raw tbody');
                  rawBody.innerHTML = data.raw.length ? data.raw.map(e => row([
                    { text: e.at, mono: true },
                    { text: e.event.id, mono: true },
                    { text: e.event.source },
                    { text: e.event.type }
                  ])).join('') : '<tr><td colspan="4" class="empty">No events yet — POST to /ingest to see one here.</td></tr>';

                  const procBody = document.querySelector('#processed tbody');
                  procBody.innerHTML = data.processed.length ? data.processed.map(e => row([
                    { text: e.at, mono: true },
                    { text: e.event.id, mono: true },
                    { text: badge(e.event.status) },
                    { text: e.event.processedBy }
                  ])).join('') : '<tr><td colspan="4" class="empty">No events yet.</td></tr>';
                } catch (err) {
                  console.error('dashboard refresh failed', err);
                }
              }
              refresh();
              setInterval(refresh, 3000);
            </script>
            </body>
            </html>
            """;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Live dashboard of recent raw and processed Kafka events")
    public String dashboard() {
        return PAGE;
    }
}
