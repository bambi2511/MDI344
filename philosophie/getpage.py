#!/usr/bin/python3
# -*- coding: utf-8 -*-

# Ne pas se soucier de ces imports
import setpath
from bs4 import BeautifulSoup
from json import loads
import re
from urllib.request import urlopen
from urllib.parse import urlencode, unquote
from functools import wraps

# Si vous écrivez des fonctions en plus, faites-le ici


class Memoize:
    def __init__(self, fn):
        self.fn = fn
        self.memo = {}

    def __call__(self, *args):
        if args not in self.memo:
            self.memo[args] = self.fn(*args)
        return self.memo[args]


def getJSON(page):
    params = urlencode({
      'format': 'json',
      'action': 'parse',
      'prop': 'text',
      'redirects': 'true',
      'page': page})
    API = "https://fr.wikipedia.org/w/api.php"
    response = urlopen(API + "?" + params)
    return response.read().decode('utf-8')


def getRawPage(page):
    parsed = loads(getJSON(page))
    # print(parsed)
    try:
        title = parsed["parse"]["title"]
        content = parsed["parse"]["text"]["*"]
        return title, content
    except KeyError:
        # La page demandée n'existe pas
        return None, None


@Memoize
def getPage(page):
    title, content = getRawPage(page)
    links = []
    pattern = re.compile(r'^/wiki/(?!.*redlink=1).*$', re.IGNORECASE)
    try:
        soup = BeautifulSoup(content, 'html.parser')
        # Fist <div> only
        # tags = soup.find_all('div', attrs={"class": "mw-parser-output"})
        tags = soup.find_all('div', recursive=False)
        # find paragraphs on first level
        tags = tags[0].find_all('p', recursive=False)
        # iterate through paragraph
        links = [unquote(a.get("href"), errors='strict')
                 for p in tags for a in p.find_all('a')]
        links = [link[6:] for link in links if pattern.match(link)]
        links = links[:10]
        return (title, links)
    except AttributeError as e:
        print(e)
        return title, None
    except TypeError as e:
        # no content given to bs
        return title, None


if __name__ == '__main__':
    # Ce code est exécuté lorsque l'on exécute le fichier
    # print("Ça fonctionne !")
    # page = "Utilisateur:A3nm/INF344"
    page = u"Muséum national d'histoire naturelle"
    # page = "Philosophie"

    # Voici des idées pour tester vos fonctions :
    print(getJSON(page))
    # print(loads(getJSON("Utilisateur:A3nm/INF344")))
    # print(getRawPage("Descartes"))
    # print(getRawPage("Histoire"))
    title, content = getPage(page)
    print(title)
    for link in content:
        print(link)
