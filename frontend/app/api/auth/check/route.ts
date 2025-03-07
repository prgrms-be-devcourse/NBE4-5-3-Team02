export async function GET() {
    return new Response(JSON.stringify({ isAuthenticated: true }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' }
    });
}