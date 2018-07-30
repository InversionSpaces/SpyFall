from twisted.internet.protocol import Factory
from twisted.protocols.basic import LineReceiver
from twisted.internet import reactor
from json import dumps, loads
import time
import random
import sqlalchemy
from sqlalchemy.orm import mapper
from sqlalchemy import *
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import *


Base = declarative_base()
engine = create_engine('sqlite:///:memorySQLITE:', echo=False)
#session = sessionmaker(bind=engine)


Session = sessionmaker(bind=engine)
session = Session()

class SQLUser(Base):
    __tablename__ = 'users'
    name = Column(String, primary_key=True)
    password = Column(String)

    def createSession(self):
        Session = sessionmaker()
        self.session = Session.configure(bind=self.engine)


    def __init__(self, name, password):
        self.name = name
        self.password = password

    def __repr__(self):
        return "<User('%s', '%s')>" % (self.name, self.password)

# Создание таблицы
Base.metadata.create_all(engine)


#aUser = SQLUser("1", "1")
#session.add(aUser)
#session.commit()

for instance in session.query(SQLUser).order_by(SQLUser.name):
    print (instance.name, instance.password)

class Room():
    name = 'noname'
    players = {}
    state = 'not running'
    sayer = 'none'
    votingperson = 'none'
    votedplayers = []
    location = -1


class Spy(LineReceiver):
    voted = False
    state = 'none'
    password = 'none'
    name = 'none'
    room = None
    ready = False
    role = 'common'

    def __init__(self, fact):
        self.factory = fact

    def connectionMade(self):
        print('connected?')

    def connectionLost(self, reason):
        print("disconnected?")

    def lineReceived(self, line):
        request = loads(line.decode('utf-8'))
        print(request)
        method = request.get("method", None)
        if method in self.factory.methods:  # it was here
            self.factory.methods[method](request, self.factory, self)
        else:
            print("Fail request: ", request)


class SpyFactory(Factory):
    rooms = {}
    players = {}

    def __init__(self):
        self.methods = dict()
        self.players = {}


    def buildProtocol(self, addr):
        return Spy(self)

    def handler(self, method):
        def retval(func):
            self.methods.update({method: func})
            return func

        return retval


fact = SpyFactory()


@fact.handler("getplayersinfo")
def getplayersinfo(req, fact, proto):
    #if (proto.state != 'inroom'):
    #    proto.sendLine(dumps({'method':'getplayers'}))
    print()
    data = dumps({'method': 'getplayersinfo', 'dict': getPlayersDict(fact.rooms[proto.room].players),
                  'array': getPlayersArray(fact.rooms[proto.room].players), 'status':'Ok'}).encode('utf-8')
    print(data)
    proto.sendLine(data)


def getPlayersArray(players):
    playersarr = []
    for player in players.keys():
        playersarr.append(player)
    return playersarr


@fact.handler("login")
def login(req, fact, proto):
    now = time.localtime(time.time())
    # _DelayedCallObj = reactor.callLater(4, print, "reactor called")
    print('req')
    data = req
    name = data['name']
    password = data['password']
    print("i've entered login")
    ourUser = session.query(SQLUser).filter_by(name=name).first()

    if (ourUser.name != name):
        proto.sendLine(dumps({'method': 'login', 'status': 'WrongLogin'}).encode('utf-8'))
        return

    if (password == ourUser.password):
        proto.state = 'inhub'
        proto.name = name
        fact.players.update ({name:proto})
        proto.sendLine(dumps({'method': 'login', 'status': 'LoggedIn'}).encode('utf-8'))
    else:
        proto.sendLine(dumps({'method': 'login', 'status': 'WrongPass'}).encode('utf-8'))


def makePlayerDict(player):
    return {player.name: player.ready}


def getPlayersDict(players):
    playersdict = dict()
    for player in players.keys():
        playersdict.update(makePlayerDict(players[player]))
    return playersdict


def makeToDict(room):
    return {'players': getPlayersDict(room.players), 'playersamount': len(room.players)}


@fact.handler("msgroomchat")
def msgroomchat(req, fact, proto):
    data = req
    name = proto.name
    msg = data['msg']
    for keyname in fact.rooms[proto.room].players.keys():
        fact.rooms[proto.room].players[keyname].sendLine(
            dumps({'method': 'newmsg', 'name': name, 'msg': msg, 'status':'Ok', 'msgtype':'public'}).encode('utf-8'))
    proto.sendLine(dumps({'method':'msgroomchat', 'status':'Ok'}).encode('utf-8'))


@fact.handler("exitroom")
def exitroom(req, fact, proto):
    #print(proto.room)
    roomname = fact.rooms[proto.room].name
    playersnum = len(fact.rooms[proto.room].players.keys())
    del fact.rooms[proto.room].players[proto.name]

    proto.sendLine(dumps({"method":"exitroom", "status":"ok"}).encode('utf-8'))
    if (len(fact.rooms[proto.room].players.keys()) <= 0):
        del fact.rooms[proto.room]
        for name in fact.players.keys():
            if (fact.players[name].state == 'inhub'):
                smsg = {'method': 'roominfo', 'status': 'DelRoom', 'roomname': roomname,
                        'playersnum': playersnum - 1}
                smsg = dumps(smsg).encode("utf-8")
                fact.players[name].sendLine(smsg)
        return


    for name in fact.players.keys():
        if (fact.players[name].state == 'inhub'):
            smsg = {'method':'roominfo', 'status':'UpdRoom', 'roomname': roomname,
                    'playersnum':playersnum - 1}
            smsg = dumps(smsg).encode("utf-8")
            fact.players[name].sendLine(smsg)

    proto.room = None
    #print(fact.rooms[proto.room])
    proto.ready = False

@fact.handler("getroomsinfo")
def getroomsinfo(req, fact, proto):
    dictionary = {}
    for room in fact.rooms.keys():
        dictionary.update({room: len(fact.rooms[room].players)})

    data = {"method": "getroomsinfo"}
    data.update({"rooms": dictionary})
    data = dumps(data).encode("utf-8")
    print(data)
    proto.sendLine(data)


@fact.handler("addroom")
def addroom(req, fact, proto):
    data = req
    name = data['roomname']

    if name in fact.rooms:
        proto.sendLine(dumps({'method': 'addroom', 'status': 'NameTaken'}).encode('utf-8'))
        return

    proto.sendLine(dumps({'method': 'addroom', 'status': 'RoomOpened'}).encode('utf-8'))

    proto.room = name

    room = Room()

    room.name = name
    room.players.update({proto.name: proto})
    fact.rooms.update({name: room})

    proto.state = "inroom"

    for name in fact.players.keys():
        if fact.players[name].state == 'inhub':
            smsg = {'method':'roominfo', 'status':'UpdRoom', 'roomname':room.name,
                    'playersnum':len(fact.rooms[proto.room].players.keys())}
            smsg = dumps(smsg).encode("utf-8")
            fact.players[name].sendLine(smsg)



@fact.handler("privatemsgroomchat")
def privatemsgroomchat(req, fact, proto):
    whom = req['whom']
    msg = req['msg']
    fact.rooms[proto.room].players[whom].sendLine(
        dumps({'method': 'newmsg', 'author': proto.name, 'msg': msg, 'msgtype':'private'}).encode('utf-8'))
    proto.sendLine(dumps({'method':'privatemsgroomchat', 'status':'Ok'}).encode('utf-8'))


@fact.handler("enterroom")
def enterroom(req, fact, proto):
    data = req
    name = data['roomname']

    if (len(fact.rooms[name].players.keys()) > 7):
        proto.sendLine(dumps({'method': 'enterroom', 'status': 'FullRoom'}).encode('utf-8'))
        return

    proto.sendLine(dumps({'method': 'enterroom', 'status': 'EnteredRoom', 'room': name}).encode('utf-8'))

    fact.rooms[name].players.update({proto.name: proto})

    proto.state = 'inroom'
    proto.room = name

    for name in fact.players.keys():
        if (fact.players[name].state == 'inhub'):
            smsg = {'method':'roominfo', 'status':'UpdRoom', 'roomname':name,
                    'playersnum':len(fact.rooms[proto.room].players.keys())}
            smsg = dumps(smsg).encode("utf-8")
            fact.players[name].sendLine(smsg)



@fact.handler("selectroomstate")
def selectroomstate(req, fact, proto):
    data = req
    state = data['state']
    if (state):
        proto.ready = True
        if (len(fact.rooms[proto.room].players.keys()) == 8):
            everyoneIsReady = True
            for keyname in fact.rooms[proto.room].players.keys():
                if (fact.rooms[proto.room].players[keyname].state == False):
                    everyoneIsReady = False
            if (everyoneIsReady):
                runGame(req, fact, proto)
    proto.sendLine(dumps({"method":"selectroomstate", "status":"Ok"}).encode('utf-8'))

# TODO complete the func

def runGame(req, fact, proto):
    DelayedCallObj = reactor.callLater(480, endGame, fact, proto)
    fact.rooms[proto.room].state = 'running'
    spyplayer = random.randint(0, 8)
    sayer = random.randint(0, 8)
    fact.rooms[proto.room].location = random.randint(0, 26)
    i = 0
    for keyname in fact.rooms[proto.room].players.keys():
        if (i == sayer):
            fact.rooms[proto.room].players[keyname].sendLine(
                dumps({'method': 'rungame', 'status': 'game begins', 'role': 'spy', 'sayer':true}).encode('utf-8'))
            fact.rooms[proto.room].sayer = keyname

        if (i == spyplayer):
            fact.rooms[proto.room].players[keyname].sendLine(
                dumps({'method': 'rungame', 'status': 'game begins', 'role': 'spy', 'sayer':false}).encode('utf-8'))
            fact.rooms.players[keyname].role = 'spy'
        else:
            fact.rooms[proto.room].players[keyname].sendLine(
                dumps({'method': 'rungame', 'status': 'game begins', 'role': 'common',
                       'location': fact.rooms[proto.room].location}).encode('utf-8'))
            fact.rooms.players[keyname].role = 'common'
        i += 1


def endGame(fact, proto):
    spyWin(fact, proto)


def spyWin(fact, proto):
    spyname = ''
    for player in fact.rooms[proto.room].players.keys():
        if (player.role == 'spy'):
            spyname = player.name

    for player1 in fact.rooms[proto.room].players.keys():
        fact.rooms[proto.room].players[player1].sendLine(
            dumps({'method': 'spyguess', 'status': 'SpyWon', 'spyname': spyname}).encode('utf-8'))

    dropAllToRoom(fact, proto)


def innocentWin(fact, proto):
    spyname = ''
    for player in fact.rooms[proto.roo,].players.keys():
        if (player.role == 'spy'):
            spyname = player.name

    for player1 in fact.rooms[proto.room].players.keys():
        fact.rooms[proto.room].players[player1].sendLine(
            dumps({'method': 'spyguess', 'status': 'InnocentWon', 'spyname': spyname}).encode('utf-8'))

    dropAllToRoom(fact, proto)


@fact.handler("spyguess")
def spyguess(req, fact, proto):
    data = req
    guess = data['location']
    if (proto.role != 'spy'):
        proto.sendLine(dumps({"method":"spyguess", "status":"WrongRole"}).encode('utf-8'))
        return
    if (guess == fact.rooms[proto.room].location):
        spyWin(fact, proto)
    else:
        innocentWin(fact, proto)


def dropAllToRoom(fact, proto):
    for player in fact.rooms[proto.room].players.keys():
        dropToRoom(fact, fact.rooms[proto.room].players[player])

@fact.handler("droptohub")
def dropToHub(req, fact, proto):
    proto.state = 'inhub'
    if (len(fact.rooms[proto.room].players.keys()) < 1):
        del fact.rooms[proto.room]
    proto.room = 'none'

    for name in fact.players.keys():
        if (fact.players[name].state == 'inhub'):
            smsg = {'method':'roominfo', 'status':'UpdRoom', 'roomname':name,
                    'playersnum':len(fact.rooms[proto.room].players.keys())}
            smsg = dumps(smsg).encode("utf-8")
            fact.players[name].sendLine(smsg)



# 'desigion':'spy|innocent'
@fact.handler("vote")
def vote(req, fact, proto):
    data = req
    person = data['whom']
    desigion = data['desigion']

    if (fact.rooms[proto.room].votingperson == 'none'):
        fact.rooms[proto.room].votingperson = person
        fact.rooms[proto.room].votedplayers.add(proto.name)
        return

    if (proto.name in fact.rooms[proto.room].votedplayers):
        proto.sendLine(dumps({'method': 'vote', 'status': 'already voted'}).encode('utf-8'))
        return

    if (person != fact.rooms[proto.room].votingperson):
        proto.sendLine(dumps({'method': 'vote', 'status': 'only one voting'}).encode('utf-8'))
        return

    if (person == proto.name):
        proto.sendLine(dumps({'method': 'vote', 'status': 'you are the object'}).encode('utf-8'))
        return

    if (desigion == 'innocent'):
        fact.rooms[proto.room].votedplayers = []
        for player in fact.rooms[proto.room].players.keys():
            fact.rooms[proto.room].players[player].sendLine(dumps({'method': 'vote',
                                                                   'status': 'over',
                                                                   'result': 'none',
                                                                   'reason': 'a player is against'}).encode('utf-8'))
        fact.rooms[proto.room].votingperson = 'none'
        return
    else:
        fact.rooms[proto.room].votedplayers.add(proto.name)
        if (len(fact.rooms[proto.room].votedplayers) > len(fact.rooms[proto.room].players) - 2):
            if (fact.rooms[proto.room].players[person].role == 'spy'):
                for player in fact.rooms[proto.room].players.keys():
                    fact.rooms[proto.room].players[player].sendLine(dumps({
                        'method': 'vote',
                        'status': 'over',
                        'result': 'spy',
                        'reason': 'all players voted'
                    }).encode('utf-8'))
                    dropToRoom(fact, fact.rooms[proto.room].players[player])
            else:
                for player in fact.rooms[proto.room].players.keys():
                    fact.rooms[proto.room].players[player].sendLine(dumps({
                        'method': 'vote',
                        'status': 'over',
                        'result': 'innocent',
                        'reason': 'all players voted'
                    }).encode('utf-8'))
                    dropToRoom(fact, fact.rooms[proto.room].players[person])


def dropToRoom(fact, proto):
    proto.state = 'inroom'
    del fact.rooms[proto.room].players[proto.name]


@fact.handler("answer")
def answer(req, fact, proto):
    pass


@fact.handler("register")
def register(req, fact, proto):
    data = req
    name = data['login']
    print("i've entered register method")
    nameexists = False
    for instance in session.query(SQLUser).order_by(SQLUser.name):
        if (name == instance.name):
            nameexists = True

    if (nameexists):
        proto.sendLine(dumps({'method': 'register', 'status': 'NameTaken'}).encode('utf-8'))
        return

    fact.players.update ({name:proto})
    proto.sendLine(dumps({'method': 'register', 'status': 'Registered'}).encode('utf-8'))

    newUser = SQLUser(name, data['password'])
    session.add(newUser)
    session.commit()

    proto.name = name
    proto.password = data['password']
    proto.state = "inhub"

    fact.players.update({name: proto})


@fact.handler("msggamechat")
def msggamechat(req, fact, proto):
    data = req
    name = proto.name
    msg = data['msg']
    succesful = True
    type = 'common'
    if (name == fact.rooms[proto.room].sayer):
        if (fact.rooms[proto.room].dialogstate == 'question'):
            if (data['msgtype'] == 'question'):
                type = 'question'
                if ((data['whom'] in fact.room[proto.room].players.keys)):
                    fact.rooms[proto.room].dialogstate = 'answer'
                    fact.rooms[proto.room].sayer = data['whom']
                    fact.rooms[proto.room].players[data['whom']].sendLine(dumps({
                        'method':'gameinfo', 'status':'YourAnswer'
                    }))
                    proto.sendLine(dumps({'method': 'msggamechat', 'status': 'Ok'}))
                else:
                    succesful = False
                    proto.sendLine(dumps({'method': 'msggamechat', 'status': 'PlayerUnknown'}))
        if (fact.rooms[proto.room].dialogstate == 'answer'):
            if (data['msgtype'] == 'answer'):
                type = 'answer'
                fact.rooms[proto.room].dialogstate = 'question'
                proto.sendLine(dumps({'method': 'msggamechat', 'status': 'Ok'}))
    else:
        proto.sendLine(dumps({'method': 'msggamechat', 'status': 'Ok'}))

    if (succesful):
        for nameiter in fact.rooms[proto.room].players.keys():
            fact.rooms[proto.room].players[nameiter].sendLine(
                dumps({'method': 'newmsggamechat', 'msg': msg, 'author': proto.name, 'type': type}).encode('utf-8'))


reactor.listenTCP(12137, fact)
reactor.run()
