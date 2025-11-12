#!/bin/bash
# Script para inicializar repo local y subir a GitHub
# USO: editar la variable REMOTE antes de ejecutar
REMOTE="git@github.com:TU_USUARIO/circuitsimm.git"

git init
git add .
git commit -m "Initial commit - circuitsimm"
git branch -M main
git remote add origin $REMOTE
echo "Repo inicializado. Ahora ejecuta: git push -u origin main"
