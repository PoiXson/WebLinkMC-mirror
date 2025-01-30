<?php declare(strict_types=1);
/*
 * PoiXson WebLinkMC - Link your website with your mc server
 * @copyright 2023-2025
 * @license AGPLv3+ADD-PXN-V1
 * @author lorenzo at poixson.com
 * @link https://poixson.com/
 */
namespace pxn\WebLinkMC;

use \pxn\WebLinkAPI\api\RequestAPI;

use \pxn\phpUtils\tools\JsonChunker;


class WebLinkAPI {

	protected $socket;



	public function __construct(string $address, int $port=25511) {
		$this->socket = \socket_create(\AF_INET, \SOCK_STREAM, \SOL_TCP);
		if ($this->socket === false)
			throw new \RuntimeException('Failed to create socket');
		\socket_set_block($this->socket);
		if (!\socket_connect($this->socket, $address, $port)) {
			if (\pxn\phpUtils\Debug::debug())
				throw new \RuntimeException('Failed to connect to: '.$address);
		}
	}



	public function sendRequest($request): void {
		$out = $request->getRequestJSON();
		\socket_write($this->socket, $out, \strlen($out));
		$chunker = new JsonChunker($request);
		while (true) {
			if ($request->isDone()) break;
			$in = \socket_read($this->socket, 2048, \PHP_BINARY_READ);
			if ($in === null ) break;
			if ($in === false) break;
			if ($in === ''   ) break;
			$chunker->process_string($in);
		}
	}



}
