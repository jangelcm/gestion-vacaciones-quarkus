from http.server import BaseHTTPRequestHandler, HTTPServer
import json

class H(BaseHTTPRequestHandler):
    def do_GET(self):
        body = b'backend ok'
        self.send_response(200)
        self.send_header('Content-Type','text/plain')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):
        length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(length) if length else b''
        try:
            data = json.loads(body) if body else {}
        except Exception:
            data = {'raw': body.decode(errors='replace')}
        resp = json.dumps({'received': data}).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type','application/json')
        self.send_header('Content-Length', str(len(resp)))
        self.end_headers()
        self.wfile.write(resp)

if __name__ == '__main__':
    server = HTTPServer(('0.0.0.0', 8081), H)
    print('Test backend listening on http://0.0.0.0:8081')
    server.serve_forever()
