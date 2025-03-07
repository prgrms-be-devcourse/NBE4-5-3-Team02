app.use(session({
    cookie: {
        secure: true,
        maxAge: null, // 브라우저 종료 시 세션 만료
        httpOnly: true
    }
}));