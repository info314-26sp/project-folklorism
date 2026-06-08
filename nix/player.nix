{
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
in

stdenv.mkDerivation {
  pname = "blackjack-player";
  version = "0.0.0";

  src = ../player;

  nativeBuildInputs = [
    jdk
    jre
    pkgs.makeWrapper
  ];

  buildPhase = ''
    runHook preBuild

    javac --source-path . PlayerTest.java
    jar cf player.jar PlayerTest.class

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    install -Dm644 player.jar $out/share/blackjack/player.jar
  
    mkdir -p $out/bin
    makeWrapper ${jre}/bin/java $out/bin/blackjack-player \
      --add-flags "-cp $out/share/blackjack/player.jar PlayerTest"

    runHook postInstall
  '';
}
