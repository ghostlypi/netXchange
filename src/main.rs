#[allow(non_snake_case)]

//Modules
mod echo_server;
mod basic_client;

//Dependencies
use std::env;
use crate::echo_server::server;
use crate::basic_client::client;


#[tokio::main]
async fn main() {
    let args: Vec<String> = env::args().collect();
    let t : &String;
    if args.len() <= 1 {
        println!("netX [type={{server,client}}]");
        return ()
    } else {
        t = &args[1];
    }

    if t == "server" {
        match server().await {
            Ok(()) => (),
            _ => println!("The Server Failed!"),
        }
    } else if t == "client" {
        match client().await {
            Ok(()) => (),
            _ => println!("The Client Failed!"),
        }
    } else {
        println!("netX [type={{server,client}}]")
    }
}