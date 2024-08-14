use tokio::net::TcpStream;
use std::io::stdin;
use nix::unistd::write;



pub async fn client() -> Result<(), Box<dyn std::error::Error>>{
    let mut stream = TcpStream::connect("127.0.0.1:31415").await?;
    let (reader,writer) = stream.into_split();
    let reader = 
    loop {
        let mut buf = String::new();
        let _ = stdin().read_line(& mut buf);
        tokio::spawn(async move {
            
        });
    }
}