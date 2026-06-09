{
  lib,
  pkgs,
  stdenv,
  ...
}:
let
  jdk = pkgs.jdk25_headless;
  jre = pkgs.jre25_minimal.override {
    jdk = jdk;
    modules = [
      "java.base"
    ];
  };

  fs = lib.fileset;
in

stdenv.mkDerivation {
  pname = "blackjack-server";
  version = "0.0.0";

  src = fs.toSource {
    root = ../.;
    fileset = fs.unions [
      ../server
      ../common
    ];
  };

  nativeBuildInputs = [
    jdk
    jre
    pkgs.makeWrapper
  ];

  buildPhase = ''
    runHook preBuild

    mv server/* .
    mv common/* .

    javac --source-path . GameServer.java
    jar cf server.jar *.class **/*.class

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    install -Dm644 server.jar $out/share/blackjack/server.jar
  
    mkdir -p $out/bin
    makeWrapper ${jre}/bin/java $out/bin/blackjack-server \
      --add-flags "-cp $out/share/blackjack/server.jar GameServer"

    runHook postInstall
  '';
}
