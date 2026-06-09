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
  pname = "blackjack-player";
  version = "0.0.0";

  src = fs.toSource {
    root = ../.;
    fileset = fs.unions [
      ../player/PlayerClient.java
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

    mv player/* .
    mv common/* .

    javac --source-path . PlayerClient.java
    jar cf player.jar *.class **/*.class

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    install -Dm644 player.jar $out/share/blackjack/player.jar
  
    mkdir -p $out/bin
    makeWrapper ${jre}/bin/java $out/bin/blackjack-player \
      --add-flags "-cp $out/share/blackjack/player.jar PlayerClient"

    runHook postInstall
  '';
}
