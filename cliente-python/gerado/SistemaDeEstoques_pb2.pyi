from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from collections.abc import Mapping as _Mapping
from typing import ClassVar as _ClassVar, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class TipoMarmita(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    TIPO_MARMITA_NAO_ESPECIFICADO: _ClassVar[TipoMarmita]
    MARMITA_P: _ClassVar[TipoMarmita]
    MARMITA_M: _ClassVar[TipoMarmita]
    MARMITA_G: _ClassVar[TipoMarmita]

class TipoInsumo(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    TIPO_INSUMO_NAO_ESPECIFICADO: _ClassVar[TipoInsumo]
    ARROZ: _ClassVar[TipoInsumo]
    FEIJAO: _ClassVar[TipoInsumo]
    MACARRAO: _ClassVar[TipoInsumo]
    CARNE: _ClassVar[TipoInsumo]
    VERDURAS: _ClassVar[TipoInsumo]
    EMBALAGEM_P: _ClassVar[TipoInsumo]
    EMBALAGEM_M: _ClassVar[TipoInsumo]
    EMBALAGEM_G: _ClassVar[TipoInsumo]
TIPO_MARMITA_NAO_ESPECIFICADO: TipoMarmita
MARMITA_P: TipoMarmita
MARMITA_M: TipoMarmita
MARMITA_G: TipoMarmita
TIPO_INSUMO_NAO_ESPECIFICADO: TipoInsumo
ARROZ: TipoInsumo
FEIJAO: TipoInsumo
MACARRAO: TipoInsumo
CARNE: TipoInsumo
VERDURAS: TipoInsumo
EMBALAGEM_P: TipoInsumo
EMBALAGEM_M: TipoInsumo
EMBALAGEM_G: TipoInsumo

class ConsultaEstoqueRequest(_message.Message):
    __slots__ = ("atraso_simulado_ms",)
    ATRASO_SIMULADO_MS_FIELD_NUMBER: _ClassVar[int]
    atraso_simulado_ms: int
    def __init__(self, atraso_simulado_ms: _Optional[int] = ...) -> None: ...

class OperacaoVaziaRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class EstoqueMarmitasData(_message.Message):
    __slots__ = ("quantidade_p", "quantidade_m", "quantidade_g")
    QUANTIDADE_P_FIELD_NUMBER: _ClassVar[int]
    QUANTIDADE_M_FIELD_NUMBER: _ClassVar[int]
    QUANTIDADE_G_FIELD_NUMBER: _ClassVar[int]
    quantidade_p: int
    quantidade_m: int
    quantidade_g: int
    def __init__(self, quantidade_p: _Optional[int] = ..., quantidade_m: _Optional[int] = ..., quantidade_g: _Optional[int] = ...) -> None: ...

class EstoqueInsumosData(_message.Message):
    __slots__ = ("porcoes_arroz", "porcoes_feijao", "porcoes_macarrao", "porcoes_carne", "porcoes_verduras", "embalagens_p", "embalagens_m", "embalagens_g")
    PORCOES_ARROZ_FIELD_NUMBER: _ClassVar[int]
    PORCOES_FEIJAO_FIELD_NUMBER: _ClassVar[int]
    PORCOES_MACARRAO_FIELD_NUMBER: _ClassVar[int]
    PORCOES_CARNE_FIELD_NUMBER: _ClassVar[int]
    PORCOES_VERDURAS_FIELD_NUMBER: _ClassVar[int]
    EMBALAGENS_P_FIELD_NUMBER: _ClassVar[int]
    EMBALAGENS_M_FIELD_NUMBER: _ClassVar[int]
    EMBALAGENS_G_FIELD_NUMBER: _ClassVar[int]
    porcoes_arroz: int
    porcoes_feijao: int
    porcoes_macarrao: int
    porcoes_carne: int
    porcoes_verduras: int
    embalagens_p: int
    embalagens_m: int
    embalagens_g: int
    def __init__(self, porcoes_arroz: _Optional[int] = ..., porcoes_feijao: _Optional[int] = ..., porcoes_macarrao: _Optional[int] = ..., porcoes_carne: _Optional[int] = ..., porcoes_verduras: _Optional[int] = ..., embalagens_p: _Optional[int] = ..., embalagens_m: _Optional[int] = ..., embalagens_g: _Optional[int] = ...) -> None: ...

class EstoqueMarmitasResponse(_message.Message):
    __slots__ = ("estoque", "thread_processamento")
    ESTOQUE_FIELD_NUMBER: _ClassVar[int]
    THREAD_PROCESSAMENTO_FIELD_NUMBER: _ClassVar[int]
    estoque: EstoqueMarmitasData
    thread_processamento: str
    def __init__(self, estoque: _Optional[_Union[EstoqueMarmitasData, _Mapping]] = ..., thread_processamento: _Optional[str] = ...) -> None: ...

class EstoqueInsumosResponse(_message.Message):
    __slots__ = ("estoque", "thread_processamento")
    ESTOQUE_FIELD_NUMBER: _ClassVar[int]
    THREAD_PROCESSAMENTO_FIELD_NUMBER: _ClassVar[int]
    estoque: EstoqueInsumosData
    thread_processamento: str
    def __init__(self, estoque: _Optional[_Union[EstoqueInsumosData, _Mapping]] = ..., thread_processamento: _Optional[str] = ...) -> None: ...

class EstoqueGeralResponse(_message.Message):
    __slots__ = ("marmitas", "insumos", "thread_processamento")
    MARMITAS_FIELD_NUMBER: _ClassVar[int]
    INSUMOS_FIELD_NUMBER: _ClassVar[int]
    THREAD_PROCESSAMENTO_FIELD_NUMBER: _ClassVar[int]
    marmitas: EstoqueMarmitasData
    insumos: EstoqueInsumosData
    thread_processamento: str
    def __init__(self, marmitas: _Optional[_Union[EstoqueMarmitasData, _Mapping]] = ..., insumos: _Optional[_Union[EstoqueInsumosData, _Mapping]] = ..., thread_processamento: _Optional[str] = ...) -> None: ...

class AjusteEstoqueMarmitaRequest(_message.Message):
    __slots__ = ("tipo", "variacao")
    TIPO_FIELD_NUMBER: _ClassVar[int]
    VARIACAO_FIELD_NUMBER: _ClassVar[int]
    tipo: TipoMarmita
    variacao: int
    def __init__(self, tipo: _Optional[_Union[TipoMarmita, str]] = ..., variacao: _Optional[int] = ...) -> None: ...

class AjusteEstoqueInsumoRequest(_message.Message):
    __slots__ = ("tipo", "variacao")
    TIPO_FIELD_NUMBER: _ClassVar[int]
    VARIACAO_FIELD_NUMBER: _ClassVar[int]
    tipo: TipoInsumo
    variacao: int
    def __init__(self, tipo: _Optional[_Union[TipoInsumo, str]] = ..., variacao: _Optional[int] = ...) -> None: ...

class DefinirEstoqueMarmitasRequest(_message.Message):
    __slots__ = ("estoque",)
    ESTOQUE_FIELD_NUMBER: _ClassVar[int]
    estoque: EstoqueMarmitasData
    def __init__(self, estoque: _Optional[_Union[EstoqueMarmitasData, _Mapping]] = ...) -> None: ...

class DefinirEstoqueInsumosRequest(_message.Message):
    __slots__ = ("estoque",)
    ESTOQUE_FIELD_NUMBER: _ClassVar[int]
    estoque: EstoqueInsumosData
    def __init__(self, estoque: _Optional[_Union[EstoqueInsumosData, _Mapping]] = ...) -> None: ...

class OperacaoEstoqueResponse(_message.Message):
    __slots__ = ("sucesso", "mensagem", "quantidade_atual", "thread_processamento")
    SUCESSO_FIELD_NUMBER: _ClassVar[int]
    MENSAGEM_FIELD_NUMBER: _ClassVar[int]
    QUANTIDADE_ATUAL_FIELD_NUMBER: _ClassVar[int]
    THREAD_PROCESSAMENTO_FIELD_NUMBER: _ClassVar[int]
    sucesso: bool
    mensagem: str
    quantidade_atual: int
    thread_processamento: str
    def __init__(self, sucesso: _Optional[bool] = ..., mensagem: _Optional[str] = ..., quantidade_atual: _Optional[int] = ..., thread_processamento: _Optional[str] = ...) -> None: ...
