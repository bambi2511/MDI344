# -*- encoding: utf-8 -*-
from bs4 import BeautifulSoup
from tasktimer import call_repeatedly
from urllib.request import urlopen
from urllib.parse import urlencode
import re


# fonction appelée périodiquement
def urlcall(toBeProcessed):
    if toBeProcessed["to_process"]:
        crawl_page(toBeProcessed["domain"],
                   toBeProcessed["to_process"],
                   toBeProcessed["processed"])
        print(toBeProcessed["to_process"])
        print(toBeProcessed["processed"])
        print("Not done yet")
        return False
    else:
        print("Done")
        return True


def getHtml(html, params):
    params = urlencode(params)
    response = urlopen(html + "?" + params)
    return response


def getContents(page):
    '''
    input:
        page: html contents (string)
    output:
        links: links matching regex (set)
    '''
    tmp_links = set()
    # pattern = re.compile(r'^/wiki/(?!.*redlink=1).*$', re.IGNORECASE)
    pattern = re.compile(r'.*[0-9]{7}.html$', re.IGNORECASE)
    try:
        soup = BeautifulSoup(page, 'html.parser')
        tmp_links = set([a.get("href") for a in soup.find_all('a') if
                         a.get("href") is not None])
        tmp_links = set([link for link in tmp_links if pattern.match(link)])
        return tmp_links
    except AttributeError as e:
        print(e)
        return tmp_links


def crawl_page(domain, to_process, processed):
    '''
    input:
        domain: root of the domain (string)
        to_process: pages that must be seen (set)
        processed: pages that were already seen (set)
    output:
       Finished: True/False
    '''
    input_link = to_process.pop()
    output_links = getContents(urlopen(domain + input_link))
    processed.add(input_link)
    # print(output_links)
    # print(processed)
    to_process = to_process | (output_links - processed)
    return to_process is None


domain = 'http://www.freepatentsonline.com'
query = domain + '/result.html'
params = {'sort': 'relevance',
          'srch': 'top',
          'query_txt': 'video',
          'submit': '',
          'patents': 'on'
          }
outputFile = "../data/search.html"

# get search contents
page = getHtml(query, params)
# get links within url
to_process = getContents(page)
processed = set()

print(to_process)
# add absolute path
# links = list(map(lambda url: domain + url, links))
# print(links)

# mise en route d'un appel toutes les 5s de la fonction urlcall avec un
# dictionnaire qui contient les paramètres passés à chaque appel de la fonction
call_repeatedly(1, 15, urlcall, {"to_process": to_process,
                                 "processed": processed,
                                 "domain": domain})
# print(len(processed))
processed = list(map(lambda url: domain + url, processed))
for link in processed:
    print(link)
