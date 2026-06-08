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
  pname = "blackjack-dealer";
  version = "0.0.0";

  src = fs.toSource {
    root = ../.;
    fileset = fs.unions [
      ../dealer
      ../Card_And_Message_Stuff
    ];
  };

  nativeBuildInputs = [
    jdk
    jre
    pkgs.makeWrapper
  ];

  buildPhase = ''
    runHook preBuild

    javac --source-path . dealer/DealerTest.java
    mv dealer/DealerTest.class .
    jar cf dealer.jar DealerTest.class Card_And_Message_Stuff/*.class

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    install -Dm644 dealer.jar $out/share/blackjack/dealer.jar
  
    mkdir -p $out/bin
    makeWrapper ${jre}/bin/java $out/bin/blackjack-dealer \
      --add-flags "-cp $out/share/blackjack/dealer.jar DealerTest"

    runHook postInstall
  '';
}
