{
  description = "development environment for info 314 final project";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    systems.url = "github:nix-systems/x86_64-linux";
  };

  outputs = {
    self,
    systems,
    nixpkgs,
  }: let
    eachSystem = nixpkgs.lib.genAttrs (import systems);
    pkgsFor = eachSystem (system:
      import nixpkgs {
        localSystem = system;
      }
    );
  in {
    devShells = eachSystem (system: {
      default = pkgsFor.${system}.mkShell {
        packages = builtins.attrValues {
          inherit (pkgsFor.${system})
            jdk25_headless
          ;
        };
      };
    });

    formatter = eachSystem (system: pkgsFor.${system}.nixfmt-tree);
  };
}
