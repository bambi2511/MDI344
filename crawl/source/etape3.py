# -*- encoding: utf-8 -*-
from bs4 import BeautifulSoup
from tasktimer import call_repeatedly
from urllib.request import urlopen
from urllib.parse import urlencode
import re


def getSearchResults(params, max_results):
    '''
    Return max_result first outputs
    '''
    patentLinks = set()
    for p in range(1, max_results):
        params['p'] = p
        searchPage = getHtml(query, params)
        tmp_set = getResultContents(searchPage)
        patentLinks = patentLinks.union(tmp_set)
    return patentLinks


def getHtml(html, params):
    params = urlencode(params)
    response = urlopen(html + "?" + params)
    return response


def getResultContents(page):
    '''
    input:
        page: html search contents (string)
    output:
        links: links matching regex (set)
    '''
    tmp_links = set()
    try:
        # soup = BeautifulSoup(page, 'html.parser')
        soup = BeautifulSoup(page, 'html5lib')
        tmp_tags = soup.find_all("div", attrs={"class": "legacy-container"})
        # tmp_tags = soup.find_all("table", attrs={"class": "listing_table"})
        tmp_links = set([a.get("href") for a in tmp_tags[0].find_all('a') if
                         a.get("href") is not None])
        return tmp_links
    except AttributeError as e:
        print(e)
        return tmp_links


domain = 'http://www.freepatentsonline.com'
query = domain + '/result.html'
params = {'sort': 'relevance',
          'srch': 'top',
          'query_txt': 'video',
          'patents': 'on'
          }
max_results = 30
outputFile = "../data/to_process"


to_process = getSearchResults(params, max_results)
to_process = list(map(lambda url: domain + url, to_process))

# write output
with open(outputFile, mode='w') as file:
    for link in to_process:
        file.write(link + '\n')
