from sdk.kernel.app.setup import create_app

app = create_app()

if __name__ == "__main__":
    import uvicorn
    from sdk.config.settings import settings
    
    uvicorn.run(
        "main:app",
        host=settings.app.host,
        port=settings.app.port,
        reload=settings.app.debug,
        timeout_keep_alive=settings.app.timeout_keep_alive,
    )
