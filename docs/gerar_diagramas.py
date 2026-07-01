import os
import urllib.request
import zlib
import base64

maketrans = bytes.maketrans(
    b'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/',
    b'0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_'
)

def plantuml_encode(text):
    zlibbed_str = zlib.compress(text.encode('utf-8'))
    compressed_string = zlibbed_str[2:-4]
    b64 = base64.b64encode(compressed_string)
    return b64.translate(maketrans).decode('utf-8')

def download_puml(puml_filename):
    base_dir = os.path.dirname(__file__)
    puml_path = os.path.join(base_dir, puml_filename)
    png_path = os.path.join(base_dir, puml_filename.replace('.puml', '.png'))

    with open(puml_path, 'r', encoding='utf-8') as f:
        content = f.read()

    encoded = plantuml_encode(content)
    url = f"http://www.plantuml.com/plantuml/png/{encoded}"

    print(f"Baixando imagem PlantUML de: {url}")
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response, open(png_path, 'wb') as out_file:
            data = response.read()
            out_file.write(data)
        print(f"Imagem gerada e salva com sucesso em: {png_path}")
    except Exception as e:
        print(f"Erro ao baixar a imagem: {e}")

def main():
    download_puml('workflow.puml')
    download_puml('casos_de_uso.puml')

if __name__ == '__main__':
    main()
